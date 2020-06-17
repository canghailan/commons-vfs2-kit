package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.File;
import cc.whohow.fs.provider.s3.S3UriPath;

import java.util.Objects;

public class AliyunOSSFile implements File<S3UriPath, AliyunOSSFile> {
    private final AliyunOSSFileSystem fileSystem;
    private final S3UriPath path;

    public AliyunOSSFile(AliyunOSSFileSystem fileSystem, S3UriPath path) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(path);
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public AliyunOSSFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public S3UriPath getPath() {
        return path;
    }

    public String getETag() {
        return (String) readAttributes().getValue(AliyunOSSFileAttributes.ETAG).orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AliyunOSSFile) {
            AliyunOSSFile that = (AliyunOSSFile) o;
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
