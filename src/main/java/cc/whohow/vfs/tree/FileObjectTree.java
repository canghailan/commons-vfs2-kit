package cc.whohow.vfs.tree;

import cc.whohow.vfs.CloudFileObject;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class FileObjectTree implements DirectoryStream<CloudFileObject> {
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
