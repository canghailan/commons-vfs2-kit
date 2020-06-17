package cc.whohow.fs.provider.file;

import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileAttributes;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

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
