package cc.whohow.vfs.tree;

import cc.whohow.vfs.FileObjectX;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class FileObjectList implements DirectoryStream<FileObjectX> {
    private final Iterable<FileObjectX> list;

    public FileObjectList(Iterable<FileObjectX> list) {
        this.list = list;
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<FileObjectX> iterator() {
        return list.iterator();
    }
}
