package cc.whohow.fs;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 虚拟文件系统
 */
public interface VirtualFileSystem extends FileManager {
    /**
     * 元数据
     */
    VirtualFileSystemMetadata getMetadata();

    /**
     * IO线程池
     */
    ExecutorService getExecutor();

    /**
     * 定时任务线程池
     */
    ScheduledExecutorService getScheduledExecutor();

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
    void load(FileSystemProvider<?, ?> provider, File metadata);

    /**
     * 挂载文件
     */
    void mount(MountPoint mountPoint);

    /**
     * 卸载文件
     */
    void umount(String path);

    /**
     * 挂载点
     */
    Collection<MountPoint> getMountPoints();
}
