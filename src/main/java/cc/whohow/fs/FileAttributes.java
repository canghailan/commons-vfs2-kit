package cc.whohow.fs;

import cc.whohow.fs.attribute.FileTimeAttribute;
import cc.whohow.fs.attribute.LongAttribute;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

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

    @Override
    default Optional<? extends Attribute<?>> get(String name) {
        Objects.requireNonNull(name);
        switch (name) {
            case SIZE:
                return Optional.of(new LongAttribute(SIZE, size()));
            case LAST_MODIFIED_TIME:
                return Optional.of(new FileTimeAttribute(LAST_MODIFIED_TIME, lastModifiedTime()));
            case CREATION_TIME:
                return Optional.of(new FileTimeAttribute(CREATION_TIME, creationTime()));
            case LAST_ACCESS_TIME:
                return Optional.of(new FileTimeAttribute(LAST_ACCESS_TIME, lastAccessTime()));
            default:
                return Optional.empty();
        }
    }

    @Override
    default Iterator<Attribute<?>> iterator() {
        return Arrays.<Attribute<?>>asList(
                new LongAttribute(SIZE, size()),
                new FileTimeAttribute(LAST_MODIFIED_TIME, lastModifiedTime()),
                new FileTimeAttribute(CREATION_TIME, creationTime()),
                new FileTimeAttribute(LAST_ACCESS_TIME, lastAccessTime())
        ).iterator();
    }
}
