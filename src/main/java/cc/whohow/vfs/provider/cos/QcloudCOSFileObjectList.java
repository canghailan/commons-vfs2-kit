package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileObjectList;

import java.util.Iterator;

public class QcloudCOSFileObjectList implements CloudFileObjectList {
    protected final QcloudCOSFileObject base;
    protected final boolean recursively;

    public QcloudCOSFileObjectList(QcloudCOSFileObject base,
                                   boolean recursively) {
        this.base = base;
        this.recursively = recursively;
    }

    @Override
    public Iterator<CloudFileObject> iterator() {
        return new QcloudCOSFileObjectIterator(base, recursively);
    }

    @Override
    public void close() {
    }
}
