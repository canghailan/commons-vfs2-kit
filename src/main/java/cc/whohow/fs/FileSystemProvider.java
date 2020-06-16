package cc.whohow.fs;

import cc.whohow.fs.io.Copy;
import cc.whohow.fs.io.Move;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface FileSystemProvider<P extends Path, F extends File<P, F>> extends AutoCloseable {
    default String getName() {
        return getClass().getName();
    }

    void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception;

    String getScheme();

    FileSystem<P, F> getFileSystem(URI uri);

    Collection<? extends FileSystem<P, F>> getFileSystems();

    ExecutorService getExecutor();

    default CompletableFuture<F> copy(F source, F target) {
        ExecutorService executor = getExecutor();
        return CompletableFuture.supplyAsync(new Copy.Parallel<>(source, target).withExecutor(executor), executor);
    }

    default CompletableFuture<F> move(F source, F target) {
        ExecutorService executor = getExecutor();
        return CompletableFuture.supplyAsync(new Move<>(new Copy.Parallel<>(source, target).withExecutor(executor)), executor);
    }
}
