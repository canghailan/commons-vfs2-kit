package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.CloudFileObject;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class QcloudCOSFileObjectList implements DirectoryStream<CloudFileObject> {
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
