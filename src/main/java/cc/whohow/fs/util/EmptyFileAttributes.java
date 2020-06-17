package cc.whohow.fs.util;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileAttributes;

import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

class EmptyFileAttributes implements FileAttributes {
    private static final EmptyFileAttributes INSTANCE = new EmptyFileAttributes();

    private EmptyFileAttributes() {
    }

    public static EmptyFileAttributes get() {
        return INSTANCE;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTimes.epoch();
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTimes.epoch();
    }

    @Override
    public FileTime creationTime() {
        return FileTimes.epoch();
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        return Optional.empty();
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return Collections.emptyIterator();
    }
}
