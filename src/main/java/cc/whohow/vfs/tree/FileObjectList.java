package cc.whohow.vfs.tree;

import cc.whohow.vfs.CloudFileObject;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class FileObjectList implements DirectoryStream<CloudFileObject> {
    private final Iterable<CloudFileObject> list;

    public FileObjectList(Iterable<CloudFileObject> list) {
        this.list = list;
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<CloudFileObject> iterator() {
        return list.iterator();
    }
}
