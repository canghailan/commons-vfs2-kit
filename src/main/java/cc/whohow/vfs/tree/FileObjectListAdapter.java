package cc.whohow.vfs.tree;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileObjectList;

import java.util.Iterator;

public class FileObjectListAdapter implements FileObjectList {
    private final Iterable<FileObject> list;

    public FileObjectListAdapter(Iterable<FileObject> list) {
        this.list = list;
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<FileObject> iterator() {
        return list.iterator();
    }
}
