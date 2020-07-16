package cc.whohow.fs;

import cc.whohow.fs.util.FileTree;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * KeyValue模型文件系统，以文件路径为Key，以文件对象为Value，仅包含文件系统运行最小必需接口
 *
 * @param <P> 文件路径
 * @param <F> 文件对象
 */
public interface FileSystem<P extends Path, F extends GenericFile<P, F>> extends ObjectFileManager {
    /**
     * 文件系统协议
     */
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
     * 文件监听服务
     */
    default FileWatchService<P, F> getWatchService() {
        throw new UnsupportedOperationException("WatchService");
    }

    /**
     * 解析文件系统内路径，使用URI.resolve来处理各种复杂情况
     *
     * @see URI#resolve(java.lang.String)
     */
    default P resolve(CharSequence path) {
        if (path.length() == 0) {
            return resolve(getUri());
        } else {
            return resolve(getUri().resolve(path.toString()));
        }
    }

    /**
     * 解析相对文件路径
     */
    default P resolve(P base, CharSequence path) {
        if (path.length() == 0) {
            return base;
        } else {
            return resolve(base.toUri().resolve(path.toString()));
        }
    }

    /**
     * 解析URI
     */
    P resolve(URI uri);

    /**
     * 获取文件对象
     */
    F get(P path);

    /**
     * 获取文件对象
     */
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
     * 文件是否存在
     */
    boolean exists(P path);

    /**
     * 删除文件（当文件不存在时，不执行任何操作）
     *
     * @see Files#delete(java.nio.file.Path)
     */
    void delete(P path);

    /**
     * 添加文件监听
     */
    default void watch(P path, FileListener listener) {
        getWatchService().watch(get(path), listener);
    }

    /**
     * 移除文件监听
     */
    default void unwatch(P path, FileListener listener) {
        getWatchService().unwatch(get(path), listener);
    }

    /**
     * 父路径
     *
     * @see java.io.File#getParent()
     */
    P getParent(P path);

    /**
     * 子文件列表
     *
     * @see Files#newDirectoryStream(java.nio.file.Path)
     */
    DirectoryStream<F> newDirectoryStream(P path);

    /**
     * 文件树（以此文件为根）
     *
     * @see Files#walk(java.nio.file.Path, int, java.nio.file.FileVisitOption...)
     */
    default FileStream<F> tree(P path) {
        return tree(path, Integer.MAX_VALUE);
    }

    /**
     * 文件树（以此文件为根）
     *
     * @see Files#walk(java.nio.file.Path, java.nio.file.FileVisitOption...)
     */
    default FileStream<F> tree(P path, int maxDepth) {
        return new FileTree<>(this, path, maxDepth);
    }

    /**
     * 读取文件属性
     *
     * @see Files#readAttributes(java.nio.file.Path, java.lang.Class, java.nio.file.LinkOption...)
     */
    FileAttributes readAttributes(P path);

    /**
     * 文件读通道
     *
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    FileReadableChannel newReadableChannel(P path);

    /**
     * 文件写通道
     *
     * @see Files#newByteChannel(java.nio.file.Path, java.nio.file.OpenOption...)
     */
    FileWritableChannel newWritableChannel(P path);
}
