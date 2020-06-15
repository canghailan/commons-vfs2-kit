package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.File;
import cc.whohow.fs.provider.s3.S3UriPath;

public class AliyunOSSFile implements File<S3UriPath, AliyunOSSFile> {
    private final AliyunOSSFileSystem fileSystem;
    private final S3UriPath path;

    public AliyunOSSFile(AliyunOSSFileSystem fileSystem, S3UriPath path) {
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
        return readAttributes().getAsString(AliyunOSSFileAttributes.ETAG).orElse("");
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
