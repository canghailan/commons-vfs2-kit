package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.command.BestMatchCommandBuilder;
import cc.whohow.fs.command.DefaultFileCopyCommand;
import cc.whohow.fs.command.DefaultFileMoveCommand;
import cc.whohow.fs.util.FileSystemThreadFactory;
import cc.whohow.fs.util.Files;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class DefaultVirtualFileSystem implements VirtualFileSystem {
    private static final Logger log = LogManager.getLogger(DefaultFileManager.class);
    protected final File<?, ?> context;
    protected final List<Provider> providers = new CopyOnWriteArrayList<>();
    protected final Map<String, String> vfsConfiguration = new LinkedHashMap<>();
    protected final NavigableMap<String, FileResolver<?, ?>> vfs = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    protected final Collection<FileSystem<?, ?>> fileSystems = new CopyOnWriteArraySet<>();
    protected final Map<String, FileCommandBuilder<? extends FileCommand<?>>> fileCommandBuilders = new ConcurrentHashMap<>();
    protected FileCommandBuilder<? extends FileCopyCommand> fileCopyCommandBuilder;
    protected FileCommandBuilder<? extends FileMoveCommand> fileMoveCommandBuilder;
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;
    protected Cache<String, File<?, ?>> cache;

    public DefaultVirtualFileSystem(File<?, ?> context) {
        this.context = context;
        this.fileCopyCommandBuilder = this::newDefaultFileCopyCommand;
        this.fileMoveCommandBuilder = this::newDefaultFileMoveCommand;
        this.initialize();
    }

    protected void initialize() {
        log.debug("initialize vfs");

        try {
            mount("meta:vfs:/", new DefaultFileResolver<>(context));
            initializeExecutor();
            initializeScheduledExecutor();
            initializeCache();
            parseVfsConfiguration();
            loadProviders();
        } catch (Exception e) {
            log.error("initialize vfs error", e);
            throw UncheckedException.unchecked(e);
        }

        log.debug("initialized");
    }

    protected synchronized void initializeExecutor() {
        log.debug("initialize executor");
        if (executor != null) {
            throw new IllegalStateException();
        }
        int corePoolSize = Files.optional(context.resolve("executor/corePoolSize"))
                .map(File::readUtf8)
                .map(Integer::parseInt)
                .orElse(8);
        int maximumPoolSize = Files.optional(context.resolve("executor/maximumPoolSize"))
                .map(File::readUtf8)
                .map(Integer::parseInt)
                .orElse(corePoolSize * 4);
        Duration keepAliveTime = Files.optional(context.resolve("executor/keepAliveTime"))
                .map(File::readUtf8)
                .map(Duration::parse)
                .orElse(Duration.ofMinutes(1));
        int maximumQueueSize = Files.optional(context.resolve("executor/maximumQueueSize"))
                .map(File::readUtf8)
                .map(Integer::parseInt)
                .orElse(corePoolSize * 32);

        this.executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime.toMillis(),
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maximumQueueSize),
                new FileSystemThreadFactory("vfs-exec-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    protected synchronized void initializeScheduledExecutor() {
        log.debug("initialize scheduler");
        if (scheduledExecutor != null) {
            throw new IllegalStateException();
        }
        int corePoolSize = Files.optional(context.resolve("scheduler/corePoolSize"))
                .map(File::readUtf8)
                .map(Integer::parseInt)
                .orElse(1);
        this.scheduledExecutor = new ScheduledThreadPoolExecutor(
                corePoolSize,
                new FileSystemThreadFactory("vfs-sched-"),
                new ThreadPoolExecutor.AbortPolicy());
    }

    protected synchronized void initializeCache() {
        log.debug("initialize cache");
        if (cache != null) {
            throw new IllegalStateException();
        }
        Duration ttl = Files.optional(context.resolve("cache/ttl"))
                .map(File::readUtf8)
                .map(Duration::parse)
                .orElse(Duration.ofMinutes(15));
        int maximumSize = Files.optional(context.resolve("cache/maximumSize"))
                .map(File::readUtf8)
                .map(Integer::parseInt)
                .orElse(1024);
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .build();
    }

    protected synchronized void parseVfsConfiguration() throws Exception {
        log.debug("parseVfsConfiguration");
        File<?, ?> vfs = context.resolve("vfs");
        if (vfs.exists()) {
            for (String line : vfs.readUtf8().split("\n")) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] keyValue = line.split(":", 2);
                vfsConfiguration.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
    }

    protected synchronized void loadProviders() throws Exception {
        log.debug("loadProviders");
        File<?, ?> providersConfiguration = context.resolve("providers/");
        if (providersConfiguration.exists()) {
            try (DirectoryStream<? extends File<?, ?>> providerConfigurations = providersConfiguration.newDirectoryStream()) {
                for (File<?, ?> providerConfiguration : providerConfigurations) {
                    String className = providerConfiguration.resolve("className").readUtf8();
                    log.debug("loadProvider: {}", className);
                    Provider provider = (Provider) Class.forName(className).newInstance();
                    provider.initialize(this, providerConfiguration);
                    providers.add(provider);
                }
            }
        }
    }

    protected Optional<? extends FileCopyCommand> newDefaultFileCopyCommand(String... arguments) {
        return Optional.of(new DefaultFileCopyCommand(this, arguments));
    }

    protected Optional<? extends FileMoveCommand> newDefaultFileMoveCommand(String... arguments) {
        return Optional.of(new DefaultFileMoveCommand(this, arguments));
    }

    @Override
    public File<?, ?> getContext() {
        return context;
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    @Override
    public Map<String, String> getVfsConfiguration() {
        return vfsConfiguration;
    }

    @Override
    public Collection<Provider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    @Override
    public Collection<FileSystem<?, ?>> getFileSystems() {
        return Collections.unmodifiableCollection(fileSystems);
    }

    @Override
    public File<?, ?> get(String uri) {
        return cache.get(uri, this::doGet);
    }

    protected File<?, ?> doGet(String uri) {
        URI normalizedUri = URI.create(uri).normalize();
        String normalized = normalizedUri.toString();
        for (Map.Entry<String, FileResolver<?, ?>> e : vfs.entrySet()) {
            if (normalized.startsWith(e.getKey())) {
                Optional<? extends File<?, ?>> file = e.getValue().resolve(
                        normalizedUri, normalized.substring(e.getKey().length()));
                if (file.isPresent()) {
                    log.trace("{} -> {}", uri, file.get());
                    return file.get();
                }
            }
        }
        throw new UncheckedException("get error: " + uri);
    }

    @Override
    public synchronized void load(Provider provider) {
        log.debug("loadProvider: {}", provider);
        try {
            File<?, ?> context = this.context.resolve("providers/" + UUID.randomUUID() + "/");
            context.resolve("className").writeUtf8(provider.getClass().getName());
            provider.initialize(this, context);
            providers.add(provider);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public synchronized void load(Provider provider, File<?, ?> configuration) {
        log.debug("loadProvider: {} {}", provider, configuration);
        try {
            File<?, ?> context = this.context.resolve("providers/" + UUID.randomUUID() + "/");
            context.resolve("className").writeUtf8(provider.getClass().getName());
            try (FileStream<? extends File<?, ?>> files = configuration.tree()) {
                for (File<?, ?> file : files) {
                    File<?, ?> copy = context.resolve(configuration.getPath().relativize(file.getPath()));
                    try (FileReadableChannel readableChannel = file.newReadableChannel();
                         FileWritableChannel writableChannel = copy.newWritableChannel()) {
                        writableChannel.transferFrom(readableChannel);
                    }
                }
            }
            provider.initialize(this, context);
            providers.add(provider);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public void mount(String uri, FileResolver<?, ?> fileResolver) {
        log.debug("mount: {}", uri);
        vfs.put(uri, fileResolver);
    }

    @Override
    public void umount(String uri) {
        log.debug("umount: {}", uri);
        vfs.remove(uri);
    }

    @Override
    public void registerFileSystem(FileSystem<?, ?> fileSystem) {
        log.debug("registerFileSystem({})", fileSystem);
        fileSystems.add(fileSystem);
    }

    @Override
    public void unregisterFileSystem(FileSystem<?, ?> fileSystem) {
        log.debug("unregisterFileSystem({})", fileSystem);
        fileSystems.remove(fileSystem);
    }

    @Override
    public synchronized void installCommand(String name, FileCommandBuilder<? extends FileCommand<?>> commandBuilder) {
        log.debug("installCommand: {}", name);
        if ("copy".equals(name) || "move".equals(name)) {
            throw new IllegalArgumentException("copy/move -> installCopyCommand/installMoveCommand");
        }
        FileCommandBuilder<? extends FileCommand<?>> fileCommandBuilder = fileCommandBuilders.get(name);
        if (fileCommandBuilder == null) {
            fileCommandBuilders.put(name, commandBuilder);
        } else {
            fileCommandBuilders.put(name, new BestMatchCommandBuilder<>(fileCommandBuilder, commandBuilder));
        }
    }

    @Override
    public synchronized void installCopyCommand(FileCommandBuilder<? extends FileCopyCommand> commandBuilder) {
        log.debug("installCopy");
        fileCopyCommandBuilder = new BestMatchCommandBuilder<>(commandBuilder, fileCopyCommandBuilder);
    }

    @Override
    public synchronized void installMoveCommand(FileCommandBuilder<? extends FileMoveCommand> commandBuilder) {
        log.debug("installMove");
        fileMoveCommandBuilder = new BestMatchCommandBuilder<>(commandBuilder, fileMoveCommandBuilder);
    }

    @Override
    public FileCommand<?> newCommand(String... args) {
        if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        String commandName = args[0];
        log.trace("newCommand: {}", commandName);
        switch (commandName) {
            case "copy":
                return fileCopyCommandBuilder.newCommand(args)
                        .orElseThrow(AssertionError::new);
            case "move":
                return fileMoveCommandBuilder.newCommand(args)
                        .orElseThrow(AssertionError::new);
            default: {
                FileCommandBuilder<? extends FileCommand<?>> commandBuilder = fileCommandBuilders.get(commandName);
                if (commandBuilder != null) {
                    Optional<? extends FileCommand<?>> command = commandBuilder.newCommand(args);
                    if (command.isPresent()) {
                        return command.get();
                    }
                }
                throw new IllegalArgumentException("command not supported: " + String.join(" ", args));
            }
        }
    }

    @Override
    public FileCopyCommand newCopyCommand(String source, String destination) {
        return fileCopyCommandBuilder.newCommand("copy", source, destination)
                .orElseThrow(AssertionError::new);
    }

    @Override
    public FileMoveCommand newMoveCommand(String source, String destination) {
        return fileMoveCommandBuilder.newCommand("move", source, destination)
                .orElseThrow(AssertionError::new);
    }

    @Override
    public void close() throws Exception {
        log.debug("close DefaultFileManager");
        for (Provider provider : providers) {
            try {
                provider.close();
            } catch (Exception e) {
                log.warn("close Provider error", e);
            }
        }
        shutdown(scheduledExecutor);
        shutdown(executor);
        log.debug("cleanUp cache");
        cache.cleanUp();
    }

    /**
     * @see ExecutorService
     */
    protected void shutdown(ExecutorService executor) {
        log.debug("shutdown {}", executor);
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    log.warn("terminate timeout: {}", executor);
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "vfs: " + vfs.keySet().toString();
    }
}
