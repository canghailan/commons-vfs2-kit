package cc.whohow.fs;

import cc.whohow.fs.util.Paths;

import java.net.URI;

/**
 * 路径，已解析的URI字符串（针对不同的文件系统，解析出特定的字段供文件系统使用）
 */
public interface Path extends Comparable<Path> {
    /**
     * 解析成URI对象
     */
    URI toUri();

    /**
     * 文件名
     */
    default String getName() {
        return Paths.getName(toUri().getPath());
    }

    /**
     * 文件扩展名
     */
    default String getExtension() {
        return Paths.getExtension(getName());
    }

    /**
     * 是否是文件
     */
    default boolean isRegularFile() {
        return !isDirectory();
    }

    /**
     * 是否是文件夹
     */
    default boolean isDirectory() {
        String path = toUri().getPath();
        return path == null || path.endsWith("/");
    }

    /**
     * 父路径
     *
     * @see java.nio.file.Path#getParent()
     */
    Path getParent();

    /**
     * 解析相对路径
     *
     * @see java.nio.file.Path#resolve(java.lang.String)
     */
    Path resolve(String relative);

    /**
     * 是否是某个路径的下级路径
     *
     * @see java.nio.file.Path#startsWith(java.nio.file.Path)
     */
    default boolean startsWith(Path path) {
        return Paths.startsWith(toUri(), path.toUri());
    }

    /**
     * 是否是某个路径的下级路径
     *
     * @see java.nio.file.Path#startsWith(java.lang.String)
     */
    default boolean startsWith(String path) {
        return Paths.startsWith(toUri(), URI.create(path));
    }

    /**
     * 生成相对路径，非下级路径将抛出异常
     *
     * @see java.nio.file.Path#relativize(java.nio.file.Path)
     */
    default String relativize(Path path) {
        return Paths.relativize(toUri(), path.toUri());
    }

    default int compareTo(Path o) {
        return toUri().compareTo(o.toUri());
    }
}
