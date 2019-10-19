package cc.whohow.vfs.tree;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.util.CloseableIterator;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.util.ArrayDeque;
import java.util.Iterator;

public class FileObjectTreeIterator implements Iterator<FileObjectX> {
    private ArrayDeque<CloseableIterator<FileObjectX>> stack = new ArrayDeque<>();

    public FileObjectTreeIterator(FileObjectX fileObject) {
        try {
            DirectoryStream<FileObjectX> list = fileObject.list();
            this.stack.push(new CloseableIterator.Adapter<>(list.iterator(), list));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasNext() {
        while (!stack.isEmpty()) {
            CloseableIterator<FileObjectX> iterator = stack.peek();
            if (iterator.hasNext()) {
                return true;
            } else {
                IO.close(iterator);
                stack.pop();
            }
        }
        return false;
    }

    @Override
    public FileObjectX next() {
        assert !stack.isEmpty();
        return stack.peek().next();
    }
}
