package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.provider.s3.S3UriPath;

public class AliyunOSSStatFile extends AliyunOSSFile {
    private final FileAttributes attributes;

    public AliyunOSSStatFile(AliyunOSSFileSystem fileSystem, S3UriPath path, FileAttributes attributes) {
        super(fileSystem, path);
        this.attributes = attributes;
    }

    @Override
    public FileAttributes readAttributes() {
        return attributes;
    }
}
