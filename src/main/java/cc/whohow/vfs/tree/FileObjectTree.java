package cc.whohow.vfs.tree;

import cc.whohow.vfs.FileObjectX;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class FileObjectTree implements DirectoryStream<FileObjectX> {
    private FileObjectX fileObject;

    public FileObjectTree(FileObjectX fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public Iterator<FileObjectX> iterator() {
        return new FileObjectTreeIterator(fileObject);
    }

    @Override
    public void close() {
    }
}
