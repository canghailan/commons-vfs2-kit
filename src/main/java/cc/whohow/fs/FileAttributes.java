package cc.whohow.fs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * 文件属性
 */
public interface FileAttributes extends Attributes {
    String LAST_MODIFIED_TIME = "lastModifiedTime";
    String LAST_ACCESS_TIME = "lastAccessTime";
    String CREATION_TIME = "creationTime";
    String SIZE = "size";

    /**
     * 最后修改时间
     *
     * @see BasicFileAttributes#lastModifiedTime()
     */
    FileTime lastModifiedTime();

    /**
     * 最后访问时间
     *
     * @see BasicFileAttributes#lastAccessTime()
     */
    FileTime lastAccessTime();

    /**
     * 创建时间
     *
     * @see BasicFileAttributes#creationTime()
     */
    FileTime creationTime();

    /**
     * 文件大小
     *
     * @see BasicFileAttributes#size()
     */
    long size();
}
