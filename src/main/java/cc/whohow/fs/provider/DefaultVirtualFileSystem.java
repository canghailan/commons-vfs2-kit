package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.io.Copy;
import cc.whohow.fs.io.Move;
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
    private static final Logger log = LogManager.getLogger(DefaultVirtualFileSystem.class);
    protected final File<?, ?> context;
    protected final Map<String, FileSystemProvider<?, ?>> providers = new ConcurrentHashMap<>();
    protected final Map<String, String> mountPoints = new LinkedHashMap<>();
    protected final NavigableMap<String, FileResolver<?, ?>> vfs = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;
    protected Cache<String, File<?, ?>> cache;

    public DefaultVirtualFileSystem(File<?, ?> context) {
        this.context = context;
        this.initialize();
    }

    protected void initialize() {
        log.debug("initialize vfs");

        try {
            mount("meta:vfs:/", new DefaultFileResolver<>(context));
            initializeExecutor();
            initializeScheduledExecutor();
            initializeCache();
            parseMountPoints();
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

    protected synchronized void parseMountPoints() throws Exception {
        log.debug("parseMountPoints");
        File<?, ?> vfs = context.resolve("vfs");
        if (vfs.exists()) {
            for (String line : vfs.readUtf8().split("\n")) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] keyValue = line.split(":", 2);
                mountPoints.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
    }

    protected synchronized void loadProviders() throws Exception {
        log.debug("loadProviders");
        File<?, ?> configurations = context.resolve("providers/");
        if (configurations.exists()) {
            try (DirectoryStream<? extends File<?, ?>> stream = configurations.newDirectoryStream()) {
                for (File<?, ?> configuration : stream) {
                    String className = configuration.resolve("className").readUtf8();
                    log.debug("loadProvider: {}", className);
                    FileSystemProvider<?, ?> provider = (FileSystemProvider<?, ?>) Class.forName(className).newInstance();
                    provider.initialize(this, configuration);
                    providers.put(provider.getScheme(), provider);
                }
            }
        }
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
    public Map<String, String> getMountPoints() {
        return mountPoints;
    }

    @Override
    public Collection<FileSystemProvider<?, ?>> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public synchronized void load(FileSystemProvider<?, ?> provider) {
        load(provider, null);
    }

    @Override
    public synchronized void load(FileSystemProvider<?, ?> provider, File<?, ?> configuration) {
        log.debug("loadProvider: {} {}", provider, configuration);
        try {
            File<?, ?> context = this.context.resolve("providers/" + UUID.randomUUID() + "/");
            context.resolve("className").writeUtf8(provider.getClass().getName());
            if (configuration != null && configuration.exists()) {
                copy(configuration, context);
            }
            provider.initialize(this, context);
            providers.put(provider.getScheme(), provider);
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
    public File<?, ?> get(String uri) {
        return cache.get(uri, this::doGet);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompletableFuture<? extends File<?, ?>> copy(File<?, ?> source, File<?, ?> target) {
        if (source.getFileSystem().getScheme().equals(target.getFileSystem().getScheme())) {
            FileSystemProvider fileSystemProvider = providers.get(source.getFileSystem().getScheme());
            return fileSystemProvider.copy(source, target);
        }
        return CompletableFuture.supplyAsync(new Copy.Parallel(source, target).withExecutor(executor), executor);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompletableFuture<? extends File<?, ?>> move(File<?, ?> source, File<?, ?> target) {
        if (source.getFileSystem().getScheme().equals(target.getFileSystem().getScheme())) {
            FileSystemProvider fileSystemProvider = providers.get(source.getFileSystem().getScheme());
            return fileSystemProvider.move(source, target);
        }
        return CompletableFuture.supplyAsync(new Move(new Copy.Parallel(source, target).withExecutor(executor)), executor);
    }

    protected File<?, ?> doGet(String uri) {
        URI normalizedUri = URI.create(uri).normalize();
        String normalized = normalizedUri.toString();
        for (Map.Entry<String, FileResolver<?, ?>> e : vfs.entrySet()) {
            if (normalized.startsWith(e.getKey())) {
                Optional<? extends File<?, ?>> file = e.getValue().resolve(
                        normalizedUri, e.getKey(), normalized.substring(e.getKey().length()));
                if (file.isPresent()) {
                    log.trace("{} -> {}", uri, file.get());
                    return file.get();
                }
            }
        }
        throw new UncheckedException("get error: " + uri);
    }

    @Override
    public void close() throws Exception {
        log.debug("close DefaultFileManager");
        for (FileSystemProvider<?, ?> provider : providers.values()) {
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
