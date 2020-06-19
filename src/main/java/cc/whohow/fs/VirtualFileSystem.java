package cc.whohow.fs;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public interface VirtualFileSystem extends AutoCloseable {
    File<?, ?> getContext();

    ExecutorService getExecutor();

    ScheduledExecutorService getScheduledExecutor();

    Map<String, String> getMountPoints();

    Collection<FileSystemProvider<?, ?>> getProviders();

    void load(FileSystemProvider<?, ?> provider);

    void load(FileSystemProvider<?, ?> provider, File<?, ?> configuration);

    void mount(String uri, FileResolver<?, ?> fileResolver);

    void umount(String uri);

    <F extends File<?, F>> File<?, F> get(String uri);

    CompletableFuture<? extends File<?, ?>> copyAsync(File<?, ?> source, File<?, ?> target);

    CompletableFuture<? extends File<?, ?>> moveAsync(File<?, ?> source, File<?, ?> target);
}
