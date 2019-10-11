package cc.whohow.vfs.tree;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileObjectList;
import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.util.CloseableIterator;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Iterator;

public class FileObjectTreeIterator implements Iterator<CloudFileObject> {
    private ArrayDeque<CloseableIterator<CloudFileObject>> stack = new ArrayDeque<>();

    public FileObjectTreeIterator(CloudFileObject fileObject) {
        try {
            CloudFileObjectList list = fileObject.list();
            this.stack.push(new CloseableIterator.Adapter<>(list.iterator(), list));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasNext() {
        while (!stack.isEmpty()) {
            CloseableIterator<CloudFileObject> iterator = stack.peek();
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
    public CloudFileObject next() {
        assert !stack.isEmpty();
        return stack.peek().next();
    }
}
