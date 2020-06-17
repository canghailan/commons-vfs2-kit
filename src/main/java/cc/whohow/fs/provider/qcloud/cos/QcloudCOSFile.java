package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.File;
import cc.whohow.fs.provider.s3.S3UriPath;

import java.util.Objects;

public class QcloudCOSFile implements File<S3UriPath, QcloudCOSFile> {
    private final QcloudCOSFileSystem fileSystem;
    private final S3UriPath path;

    public QcloudCOSFile(QcloudCOSFileSystem fileSystem, S3UriPath path) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(path);
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
        return (String) readAttributes().getValue(QcloudCOSFileAttributes.ETAG).orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof QcloudCOSFile) {
            QcloudCOSFile that = (QcloudCOSFile) o;
            return fileSystem.equals(that.fileSystem) &&
                    path.equals(that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, path);
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
