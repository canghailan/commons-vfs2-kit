package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.provider.s3.S3UriPath;

public class QcloudCOSStatFile extends QcloudCOSFile {
    private final FileAttributes attributes;

    public QcloudCOSStatFile(QcloudCOSFileSystem fileSystem, S3UriPath path, FileAttributes attributes) {
        super(fileSystem, path);
        this.attributes = attributes;
    }

    @Override
    public FileAttributes readAttributes() {
        return attributes;
    }
}
