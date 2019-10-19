package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.FileObjectX;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class AliyunOSSFileObjectList implements DirectoryStream<FileObjectX> {
    protected final AliyunOSSFileObject base;
    protected final boolean recursively;

    public AliyunOSSFileObjectList(AliyunOSSFileObject base,
                                   boolean recursively) {
        this.base = base;
        this.recursively = recursively;
    }

    @Override
    public Iterator<FileObjectX> iterator() {
        return new AliyunOSSFileObjectIterator(base, recursively);
    }

    @Override
    public void close() {
    }
}
