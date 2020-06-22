package cc.whohow.fs;


import cc.whohow.fs.util.FileTimes;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 文件对象
 *
 * @param <P> 文件路径
 * @param <F> 文件对象（本身）
 */
public interface File<P extends Path, F extends File<P, F>> extends ObjectFile {
    /**
     * 文件系统
     */
    FileSystem<P, F> getFileSystem();

    /**
     * 文件路径
     */
    P getPath();

    // 以下为代理方法

    /**
     * 文件存储URI
     */
    default URI getUri() {
        return getPath().toUri();
    }

    /**
     * 文件公开URI，一般使用此URI
     */
    default String getPublicUri() {
        return getFileSystem().getPublicUri(getPath());
    }

    /**
     * 文件所有URI集合
     */
    default Collection<String> getUris() {
        return getFileSystem().getUris(getPath());
    }

    /**
     * 文件名
     */
    default String getName() {
        return getPath().getName();
    }

    /**
     * 文件扩展名，无扩展名返回空字符串
     */
    default String getExtension() {
        return getPath().getExtension();
    }

    /**
     * 是否是文件
     *
     * @see Files#isRegularFile(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean isRegularFile() {
        return getPath().isRegularFile();
    }

    /**
     * 是否是文件夹
     *
     * @see Files#isDirectory(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean isDirectory() {
        return getPath().isDirectory();
    }

    /**
     * 文件是否存在
     *
     * @see Files#exists(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean exists() {
        return getFileSystem().exists(getPath());
    }

    /**
     * 读取文件属性
     *
     * @see Files#readAttributes(java.nio.file.Path, java.lang.Class, java.nio.file.LinkOption...)
     */
    default FileAttributes readAttributes() {
        return getFileSystem().readAttributes(getPath());
    }

    /**
     * 获取文件/文件夹最后修改时间
     *
     * @see Files#getLastModifiedTime(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default FileTime getLastModifiedTime() {
        if (isDirectory()) {
            try (Stream<F> stream = tree().stream()) {
                return stream
                        .filter(F::isRegularFile)
                        .map(F::getLastModifiedTime)
                        .max(FileTime::compareTo)
                        .orElse(FileTimes.epoch());
            }
        } else {
            return readAttributes().lastModifiedTime();
        }
    }

    /**
     * 获取文件/文件夹大小
     *
     * @see Files#size(java.nio.file.Path)
     */
    default long size() {
        if (isDirectory()) {
            try (Stream<F> stream = tree().stream()) {
                return stream
                        .filter(F::isRegularFile)
                        .mapToLong(F::size)
                        .sum();
            }
        } else {
            return readAttributes().size();
        }
    }

    /**
     * 文件读通道
     *
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    default FileReadableChannel newReadableChannel() {
        return getFileSystem().newReadableChannel(getPath());
    }

    /**
     * 文件写通道
     *
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    default FileWritableChannel newWritableChannel() {
        return getFileSystem().newWritableChannel(getPath());
    }

    /**
     * 父文件夹
     *
     * @see java.io.File#getParent()
     */
    default F getParent() {
        P parent = getFileSystem().getParent(getPath());
        if (parent == null) {
            return null;
        }
        return getFileSystem().get(parent);
    }

    /**
     * 子文件列表
     *
     * @see Files#newDirectoryStream(java.nio.file.Path)
     */
    default DirectoryStream<F> newDirectoryStream() {
        return getFileSystem().newDirectoryStream(getPath());
    }

    /**
     * 文件树（以此文件为根）
     *
     * @see Files#walk(java.nio.file.Path, int, java.nio.file.FileVisitOption...)
     */
    default FileStream<F> tree() {
        return getFileSystem().tree(getPath());
    }

    /**
     * 文件树（以此文件为根）
     *
     * @see Files#walk(java.nio.file.Path, java.nio.file.FileVisitOption...)
     */
    default FileStream<F> tree(int maxDepth) {
        return getFileSystem().tree(getPath(), maxDepth);
    }

    /**
     * 删除文件/文件夹（当文件/文件夹不存在时，不执行任何操作）
     *
     * @see Files#delete(java.nio.file.Path)
     */
    default void delete() {
        getFileSystem().delete(getPath());
    }

    /**
     * 添加文件监听
     */
    default void watch(Consumer<FileWatchEvent<P, F>> listener) {
        getFileSystem().watch(getPath(), listener);
    }

    /**
     * 移除文件监听
     */
    default void unwatch(Consumer<FileWatchEvent<P, F>> listener) {
        getFileSystem().unwatch(getPath(), listener);
    }

    /**
     * 解析下级文件
     */
    default F resolve(CharSequence path) {
        return getFileSystem().get(getFileSystem().resolve(getPath(), path));
    }
}
