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

    default URI getUri() {
        return getPath().toUri();
    }

    default String getPublicUri() {
        return getFileSystem().getPublicUri(getPath());
    }

    default Collection<String> getUris() {
        return getFileSystem().getUris(getPath());
    }

    default String getName() {
        return getPath().getName();
    }

    /**
     * @see Files#isRegularFile(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean isRegularFile() {
        return getPath().isRegularFile();
    }

    /**
     * @see Files#isDirectory(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean isDirectory() {
        return getPath().isDirectory();
    }

    /**
     * @see Files#exists(java.nio.file.Path, java.nio.file.LinkOption...)
     */
    default boolean exists() {
        return getFileSystem().exists(getPath());
    }

    /**
     * @see Files#readAttributes(java.nio.file.Path, java.lang.Class, java.nio.file.LinkOption...)
     */
    default FileAttributes readAttributes() {
        return getFileSystem().readAttributes(getPath());
    }

    /**
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
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    default FileReadableChannel newReadableChannel() {
        return getFileSystem().newReadableChannel(getPath());
    }

    /**
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    default FileWritableChannel newWritableChannel() {
        return getFileSystem().newWritableChannel(getPath());
    }

    /**
     * @see Files#newDirectoryStream(java.nio.file.Path)
     */
    default DirectoryStream<F> newDirectoryStream() {
        return getFileSystem().newDirectoryStream(getPath());
    }

    /**
     * @see Files#walk(java.nio.file.Path, int, java.nio.file.FileVisitOption...)
     */
    default FileStream<F> tree() {
        return getFileSystem().tree(getPath());
    }

    /**
     * @see Files#walk(java.nio.file.Path, java.nio.file.FileVisitOption...)
     */
    default FileStream<F> tree(int maxDepth) {
        return getFileSystem().tree(getPath(), maxDepth);
    }

    /**
     * @see Files#delete(java.nio.file.Path)
     */
    default void delete() {
        getFileSystem().delete(getPath());
    }

    default void watch(Consumer<FileWatchEvent<P, F>> listener) {
        getFileSystem().watch(getPath(), listener);
    }

    default void unwatch(Consumer<FileWatchEvent<P, F>> listener) {
        getFileSystem().unwatch(getPath(), listener);
    }

    default File<P, F> resolve(CharSequence relative) {
        return getFileSystem().get(getFileSystem().resolve(getPath(), relative));
    }
}
