package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.FileStream;
import cc.whohow.fs.provider.s3.S3UriPath;

import java.io.IOException;
import java.util.Iterator;

public class QcloudCOSFileTree implements FileStream<QcloudCOSFile> {
    private final QcloudCOSFileSystem fileSystem;
    private final S3UriPath path;
    private final boolean recursively;

    public QcloudCOSFileTree(QcloudCOSFileSystem fileSystem, S3UriPath path, boolean recursively) {
        this.fileSystem = fileSystem;
        this.path = path;
        this.recursively = recursively;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Iterator<QcloudCOSFile> iterator() {
        return new QcloudCOSFileIterator(fileSystem, path, recursively);
    }
}
