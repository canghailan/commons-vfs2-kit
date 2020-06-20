package cc.whohow.fs;

import cc.whohow.fs.provider.AsyncCopy;
import cc.whohow.fs.provider.CopyAndDelete;
import cc.whohow.fs.provider.ProviderCopy;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 文件系统SPI，负责根据配置文件初始化FileSystem，同时提供同类型文件Copy、Move操作的实现
 */
public interface FileSystemProvider<P extends Path, F extends File<P, F>> extends AutoCloseable {
    default String getName() {
        return getClass().getName();
    }

    /**
     * 初始化
     */
    void initialize(VirtualFileSystem vfs, File<?, ?> metadata) throws Exception;

    /**
     * 文件协议
     */
    String getScheme();

    /**
     * 获取URI所属文件系统
     */
    FileSystem<P, F> getFileSystem(URI uri);

    /**
     * 获取所有初始化文件系统集合
     */
    Collection<? extends FileSystem<P, F>> getFileSystems();

    /**
     * IO线程池
     */
    ExecutorService getExecutor();

    /**
     * 复制文件
     */
    default CompletableFuture<F> copyAsync(F source, F target) {
        return CompletableFuture.supplyAsync(
                new AsyncCopy<>(source, target, getExecutor()), getExecutor())
                .join();
    }

    /**
     * 剪切文件
     */
    default CompletableFuture<F> moveAsync(F source, F target) {
        return CompletableFuture.supplyAsync(
                new CopyAndDelete<>(new ProviderCopy<>(this, source, target)), getExecutor())
                .join();
    }
}
