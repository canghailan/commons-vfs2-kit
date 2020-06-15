package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.File;
import cc.whohow.fs.provider.s3.S3UriPath;

public class QcloudCOSFile implements File<S3UriPath, QcloudCOSFile> {
    private final QcloudCOSFileSystem fileSystem;
    private final S3UriPath path;

    public QcloudCOSFile(QcloudCOSFileSystem fileSystem, S3UriPath path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public QcloudCOSFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public S3UriPath getPath() {
        return path;
    }

    public String getETag() {
        return readAttributes().getAsString(QcloudCOSFileAttributes.ETAG).orElse("");
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
