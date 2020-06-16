package cc.whohow.fs;

import cc.whohow.fs.util.FileTree;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/**
 * KeyValue模型文件系统，以文件路径为Key，以文件对象为Value，仅包含文件系统运行最小必需接口
 *
 * @param <P> 文件路径
 * @param <F> 文件对象
 */
public interface FileSystem<P extends Path, F extends File<P, F>> extends ObjectFileManager {
    default String getScheme() {
        return getUri().getScheme();
    }

    /**
     * 文件系统ID
     */
    URI getUri();

    /**
     * 文件系统ID
     */
    default Set<String> getUris() {
        return Collections.singleton(getUri().toString());
    }

    /**
     * 文件系统属性
     */
    FileSystemAttributes readAttributes();

    /**
     * @see URI#resolve(java.lang.String)
     * 解析文件路径，默认应用此方法解析，复用URI.resolve来处理各种复杂情况
     */
    default P resolve(CharSequence path) {
        return resolve(getUri().resolve(path.toString()));
    }

    /**
     * 解析URI
     */
    P resolve(URI uri);

    /**
     * 获取上级路径
     */
    P getParent(P path);

    /**
     * 解析相对文件路径
     */
    default P resolve(P base, CharSequence path) {
        return resolve(base.toUri().resolve(path.toString()));
    }

    /**
     * 文件是否存在
     */
    boolean exists(P path);

    /**
     * 获取文件对象
     */
    F get(P path);

    default F get(CharSequence path) {
        return get(resolve(path));
    }

    /**
     * 获取文件对象
     */
    default F get(URI uri) {
        return get(resolve(uri));
    }

    /**
     * 获取文件公开URI
     */
    default String getPublicUri(P path) {
        return path.toUri().toString();
    }

    /**
     * 获取文件所有URI
     */
    default Collection<String> getUris(P path) {
        return Collections.singleton(path.toUri().toString());
    }

    /**
     * 获取文件属性
     */
    FileAttributes readAttributes(P path);

    /**
     * 文件读通道
     */
    FileReadableChannel newReadableChannel(P path);

    /**
     * 文件写通道
     */
    FileWritableChannel newWritableChannel(P path);

    /**
     * 目录文件流
     */
    DirectoryStream<F> newDirectoryStream(P path);

    /**
     * 删除文件
     */
    void delete(P path);

    default FileWatchService<P, F> getWatchService() {
        return null;
    }

    default void watch(P path, Consumer<FileWatchEvent<P, F>> listener) {
        FileWatchService<P, F> watchService = getWatchService();
        if (watchService != null) {
            watchService.watch(get(path), listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    default void unwatch(P path, Consumer<FileWatchEvent<P, F>> listener) {
        FileWatchService<P, F> watchService = getWatchService();
        if (watchService != null) {
            watchService.watch(get(path), listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 文件树
     */
    default FileStream<F> tree(P path) {
        return tree(path, Integer.MAX_VALUE);
    }

    /**
     * 文件集（本级及下级）
     */
    default FileStream<F> tree(P path, int maxDepth) {
        return new FileTree<>(this, path, maxDepth);
    }
}
