package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.FileStream;
import cc.whohow.fs.provider.s3.S3UriPath;

import java.io.IOException;
import java.util.Iterator;

public class AliyunOSSFileTree implements FileStream<AliyunOSSFile> {
    private final AliyunOSSFileSystem fileSystem;
    private final S3UriPath path;
    private final boolean recursively;

    public AliyunOSSFileTree(AliyunOSSFileSystem fileSystem, S3UriPath path, boolean recursively) {
        this.fileSystem = fileSystem;
        this.path = path;
        this.recursively = recursively;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Iterator<AliyunOSSFile> iterator() {
        return new AliyunOSSFileIterator(fileSystem, path, recursively);
    }
}
