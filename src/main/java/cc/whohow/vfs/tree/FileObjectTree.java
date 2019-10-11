package cc.whohow.vfs.tree;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileObjectList;

import java.util.Iterator;

public class FileObjectTree implements CloudFileObjectList {
    private CloudFileObject fileObject;

    public FileObjectTree(CloudFileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public Iterator<CloudFileObject> iterator() {
        return new FileObjectTreeIterator(fileObject);
    }

    @Override
    public void close() {
    }
}
