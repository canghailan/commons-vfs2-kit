package cc.whohow.vfs.tree;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileObjectList;

import java.util.Iterator;

public class FileObjectTree implements FileObjectList {
    private FileObject fileObject;

    public FileObjectTree(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public Iterator<FileObject> iterator() {
        return new FileObjectTreeIterator(fileObject);
    }

    @Override
    public void close() {
    }
}
