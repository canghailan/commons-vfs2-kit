package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.util.FileSystemThreadFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class DefaultVirtualFileSystem implements VirtualFileSystem {
    private static final Logger log = LogManager.getLogger(DefaultVirtualFileSystem.class);
    protected final DefaultVirtualFileSystemMetadata metadata;
    protected final Map<String, FileSystemProvider<?, ?>> providers = new ConcurrentHashMap<>();
    // 可使用字典树Trie优化
    protected final NavigableMap<String, MountPoint> vfs = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;
    protected Cache<String, File> cache;

    public DefaultVirtualFileSystem(File metadata) {
        this.metadata = new DefaultVirtualFileSystemMetadata(metadata);
        this.initialize();
    }

    protected void initialize() {
        log.debug("initialize vfs");

        try {
            mount(new FileBasedMountPoint("meta:vfs:/", metadata.getFileMetadata()));
            initializeExecutor();
            initializeScheduledExecutor();
            initializeCache();
            loadProviders();
        } catch (Exception e) {
            log.error("initialize vfs error", e);
            throw UncheckedException.unchecked(e);
        }

        log.debug("initialized");
    }

    protected synchronized void initializeExecutor() {
        log.debug("initialize executor");
        if (this.executor != null) {
            throw new IllegalStateException();
        }

        int corePoolSize = metadata.getInteger("executor/corePoolSize")
                .orElse(Runtime.getRuntime().availableProcessors());
        int maximumPoolSize = metadata.getInteger("executor/maximumPoolSize")
                .orElse(corePoolSize * 8);
        Duration keepAliveTime = metadata.getDuration("executor/keepAliveTime")
                .orElse(Duration.ofMinutes(1));
        int maximumQueueSize = metadata.getInteger("executor/maximumQueueSize")
                .orElse(maximumPoolSize * 8);

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
        if (this.scheduledExecutor != null) {
            throw new IllegalStateException();
        }

        int corePoolSize = metadata.getInteger("scheduler/corePoolSize")
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
        Duration ttl = metadata.getDuration("cache/ttl")
                .orElse(Duration.ofMinutes(15));
        int maximumSize = metadata.getInteger("cache/maximumSize")
                .orElse(4096);
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .build();
    }

    protected synchronized void loadProviders() throws Exception {
        log.debug("loadProviders");
        File configurations = resolveMetadata("providers/");
        if (configurations.exists()) {
            try (DirectoryStream<? extends File> stream = configurations.newDirectoryStream()) {
                for (File configuration : stream) {
                    String className = configuration.resolve("className").readUtf8();
                    log.debug("loadProvider: {}", className);
                    FileSystemProvider<?, ?> provider = (FileSystemProvider<?, ?>) Class.forName(className).newInstance();
                    provider.initialize(this, configuration);
                    providers.put(provider.getScheme(), provider);
                }
            }
        }
    }

    protected File resolveMetadata(String path) {
        return metadata.getFileMetadata().resolve(path);
    }

    @Override
    public VirtualFileSystemMetadata getMetadata() {
        return metadata;
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
    public Collection<FileSystemProvider<?, ?>> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public synchronized void load(FileSystemProvider<?, ?> provider) {
        load(provider, null);
    }

    @Override
    public synchronized void load(FileSystemProvider<?, ?> provider, File metadata) {
        log.debug("loadProvider: {} {}", provider, metadata);
        try {
            File configuration = resolveMetadata("providers/" + UUID.randomUUID() + "/");
            configuration.resolve("className").writeUtf8(provider.getClass().getName());
            if (metadata != null && metadata.exists()) {
                copyAsync(metadata, configuration).join();
            }
            provider.initialize(this, configuration);
            providers.put(provider.getScheme(), provider);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public void mount(MountPoint mountPoint) {
        log.debug("mount: {}", mountPoint);
        vfs.put(mountPoint.getPath(), mountPoint);
    }

    @Override
    public void umount(String path) {
        log.debug("umount: {}", path);
        vfs.remove(path);
    }

    @Override
    public File get(CharSequence uri) {
        return cache.get(uri.toString(), this::doGet);
    }

    protected File doGet(String uri) {
        URI normalizedUri = URI.create(uri).normalize();
        for (MountPoint mountPoint : vfs.values()) {
            Optional<? extends File> file = mountPoint.resolve(normalizedUri);
            if (file.isPresent()) {
                log.trace("{} -> {}", uri, file.get());
                return file.get();
            }
        }
        throw new UncheckedIOException(new FileNotFoundException(uri));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompletableFuture<? extends File> copyAsync(File source, File target) {
        FileSystemProvider sourceProvider = getProvider(source);
        FileSystemProvider targetProvider = getProvider(target);
        if (Objects.equals(sourceProvider, targetProvider)) {
            return sourceProvider.copy((GenericFile) source, (GenericFile) target).callAsync(executor);
        } else {
            return new FileCopy(source, target).callAsync(executor);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompletableFuture<? extends File> moveAsync(File source, File target) {
        FileSystemProvider sourceProvider = getProvider(source);
        FileSystemProvider targetProvider = getProvider(target);
        if (Objects.equals(sourceProvider, targetProvider)) {
            return sourceProvider.move((GenericFile) source, (GenericFile) target).callAsync(executor);
        } else {
            return new CopyAndDelete<>(new FileCopy(source, target)).callAsync(executor);
        }
    }

    protected FileSystemProvider<?, ?> getProvider(File file) {
        return providers.get(file.getUri().getScheme());
    }

    @Override
    public void close() throws Exception {
        log.debug("close DefaultVirtualFileSystem");
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
