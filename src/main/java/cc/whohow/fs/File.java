package cc.whohow.fs;

import cc.whohow.fs.util.FileTimes;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @see java.io.File
 */
public interface File extends ObjectFile {
    /**
     * 文件路径
     */
    Path getPath();

    /**
     * 文件标准URI
     */
    default URI getUri() {
        return getPath().toUri();
    }

    /**
     * 文件公开URI，一般使用此URI
     */
    String getPublicUri();

    /**
     * 文件所有URI集合
     */
    Collection<String> getUris();

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
     * 文件是否存在
     *
     * @see Files#exists(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    boolean exists();

    /**
     * 删除文件/文件夹（当文件/文件夹不存在时，不执行任何操作）
     *
     * @see Files#delete(java.nio.file.Path)
     */
    void delete();

    /**
     * 获取文件/文件夹最后修改时间
     *
     * @see Files#getLastModifiedTime(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default FileTime getLastModifiedTime() {
        if (isDirectory()) {
            try (Stream<? extends File> stream = tree().stream()) {
                return stream
                        .filter(File::isRegularFile)
                        .map(File::getLastModifiedTime)
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
            try (Stream<? extends File> stream = tree().stream()) {
                return stream
                        .filter(File::isRegularFile)
                        .mapToLong(File::size)
                        .sum();
            }
        } else {
            return readAttributes().size();
        }
    }

    /**
     * 添加文件监听
     */
    void watch(FileListener listener);

    /**
     * 移除文件监听
     */
    void unwatch(FileListener listener);

    /**
     * 父文件夹
     *
     * @see java.io.File#getParent()
     */
    File getParent();

    // --- 以下是文件夹方法 --- //

    /**
     * 是否是文件夹
     *
     * @see java.io.File#isDirectory()
     * @see Files#isDirectory(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean isDirectory() {
        return getPath().isDirectory();
    }

    /**
     * 子文件列表
     *
     * @see java.io.File#listFiles()
     * @see Files#newDirectoryStream(java.nio.file.Path)
     */
    DirectoryStream<? extends File> newDirectoryStream();

    /**
     * 文件树（以此文件为根）
     *
     * @see Files#walk(java.nio.file.Path, int, java.nio.file.FileVisitOption...)
     */
    FileStream<? extends File> tree();

    /**
     * 文件树（以此文件为根）
     *
     * @see Files#walk(java.nio.file.Path, java.nio.file.FileVisitOption...)
     */
    FileStream<? extends File> tree(int maxDepth);

    /**
     * 解析下级文件
     */
    File resolve(CharSequence path);

    // --- 以下是文件方法 --- //

    /**
     * 是否是文件
     *
     * @see Files#isRegularFile(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean isRegularFile() {
        return getPath().isRegularFile();
    }

    /**
     * 读取文件属性
     *
     * @see Files#readAttributes(java.nio.file.Path, java.lang.Class, java.nio.file.LinkOption...)
     */
    FileAttributes readAttributes();

    /**
     * 文件读通道
     *
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    FileReadableChannel newReadableChannel();

    /**
     * 文件写通道
     *
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    FileWritableChannel newWritableChannel();
}
