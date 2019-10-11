package cc.whohow.vfs.tree;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileObjectList;

import java.util.Iterator;

public class FileObjectListAdapter implements CloudFileObjectList {
    private final Iterable<CloudFileObject> list;

    public FileObjectListAdapter(Iterable<CloudFileObject> list) {
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
