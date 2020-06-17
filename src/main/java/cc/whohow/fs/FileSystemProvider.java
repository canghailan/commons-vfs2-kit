package cc.whohow.fs;

import cc.whohow.fs.provider.CopyAndDelete;
import cc.whohow.fs.provider.ProviderCopy;
import cc.whohow.fs.provider.StreamCopy;

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

    default CompletableFuture<F> copyAsync(F source, F target) {
        return CompletableFuture.supplyAsync(
                new StreamCopy.Parallel<>(source, target).withExecutor(getExecutor()), getExecutor());
    }

    default CompletableFuture<F> moveAsync(F source, F target) {
        return CompletableFuture.supplyAsync(
                new CopyAndDelete<>(new ProviderCopy<>(this, source, target)), getExecutor());
    }
}
