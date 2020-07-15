package cc.whohow.fs;

import cc.whohow.fs.provider.CopyAndDelete;
import cc.whohow.fs.provider.GenericFileCopy;

import java.net.URI;
import java.util.Collection;

/**
 * 文件系统SPI，负责根据配置文件初始化FileSystem，同时提供同类型文件Copy、Move操作的实现
 */
public interface FileSystemProvider<P extends Path, F extends GenericFile<P, F>> extends AutoCloseable {
    default String getName() {
        return getClass().getName();
    }

    /**
     * 初始化
     */
    void initialize(VirtualFileSystem vfs, File metadata) throws Exception;

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
     * 复制文件
     */
    default Copy<F, F> copy(F source, F target) {
        return new GenericFileCopy<>(source, target);
    }

    /**
     * 剪切文件
     */
    default Move<F, F> move(F source, F target) {
        return new CopyAndDelete<>(copy(source, target));
    }
}
