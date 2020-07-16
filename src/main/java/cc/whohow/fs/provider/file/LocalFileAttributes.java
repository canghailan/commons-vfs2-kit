package cc.whohow.fs.provider.file;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.attribute.FileTimeAttribute;
import cc.whohow.fs.attribute.LongAttribute;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class LocalFileAttributes implements FileAttributes {
    private final BasicFileAttributes fileAttributes;

    public LocalFileAttributes(BasicFileAttributes fileAttributes) {
        this.fileAttributes = fileAttributes;
    }

    @Override
    public FileTime lastModifiedTime() {
        return fileAttributes.lastModifiedTime();
    }

    @Override
    public FileTime lastAccessTime() {
        return fileAttributes.lastAccessTime();
    }

    @Override
    public FileTime creationTime() {
        return fileAttributes.creationTime();
    }

    @Override
    public long size() {
        return fileAttributes.size();
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
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
    public Iterator<Attribute<?>> iterator() {
        return Arrays.<Attribute<?>>asList(
                new LongAttribute(SIZE, size()),
                new FileTimeAttribute(LAST_MODIFIED_TIME, lastModifiedTime()),
                new FileTimeAttribute(CREATION_TIME, creationTime()),
                new FileTimeAttribute(LAST_ACCESS_TIME, lastAccessTime())
        ).iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof LocalFileAttributes) {
            LocalFileAttributes that = (LocalFileAttributes) o;
            return fileAttributes.equals(that.fileAttributes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileAttributes.hashCode();
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
