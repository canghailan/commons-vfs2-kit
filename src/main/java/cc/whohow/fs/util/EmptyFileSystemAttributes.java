package cc.whohow.fs.util;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileSystemAttributes;

import java.util.Collections;
import java.util.Iterator;

class EmptyFileSystemAttributes implements FileSystemAttributes {
    private static final EmptyFileSystemAttributes INSTANCE = new EmptyFileSystemAttributes();

    private EmptyFileSystemAttributes() {
    }

    public static EmptyFileSystemAttributes get() {
        return INSTANCE;
    }

    @Override
    public String provider() {
        return "";
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return Collections.emptyIterator();
    }
}
