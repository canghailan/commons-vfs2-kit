package cc.whohow.fs;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 虚拟文件系统
 */
public interface VirtualFileSystem extends AutoCloseable {
    /**
     * 元数据
     */
    File<?, ?> getMetadata();

    /**
     * IO线程池
     */
    ExecutorService getExecutor();

    /**
     * 定时任务线程池
     */
    ScheduledExecutorService getScheduledExecutor();

    /**
     * 挂载点
     */
    Map<String, String> getMountPoints();

    /**
     * 已加载文件系统SPI集合
     */
    Collection<FileSystemProvider<?, ?>> getProviders();

    /**
     * 加载文件系统SPI
     */
    void load(FileSystemProvider<?, ?> provider);

    /**
     * 加载文件系统SPI
     */
    void load(FileSystemProvider<?, ?> provider, File<?, ?> metadata);

    /**
     * 挂载文件
     */
    void mount(String uri, FileResolver<?, ?> fileResolver);

    /**
     * 卸载文件
     */
    void umount(String uri);

    /**
     * 获取文件（寻址）
     */
    <F extends File<?, F>> File<?, F> get(String uri);

    /**
     * 拷贝文件
     */
    CompletableFuture<? extends File<?, ?>> copyAsync(File<?, ?> source, File<?, ?> target);

    /**
     * 移动文件
     */
    CompletableFuture<? extends File<?, ?>> moveAsync(File<?, ?> source, File<?, ?> target);
}
