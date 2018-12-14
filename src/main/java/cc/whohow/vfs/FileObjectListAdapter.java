package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public class FileObjectListAdapter implements FileObjectList {
    private final Iterable<FileObject> list;
    private final Closeable closeable;

    public FileObjectListAdapter(Iterable<FileObject> list) {
        this(list, null);
    }

    public FileObjectListAdapter(Iterable<FileObject> list, Closeable closeable) {
        this.list = list;
        this.closeable = closeable;
    }

    @Override
    public Iterator<FileObject> iterator() {
        return list.iterator();
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }
}
