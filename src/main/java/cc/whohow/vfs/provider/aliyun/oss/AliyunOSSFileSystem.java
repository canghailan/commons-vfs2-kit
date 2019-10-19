package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.FileSystemX;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.provider.s3.S3FileName;
import com.aliyun.oss.OSS;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

public class AliyunOSSFileSystem extends AbstractVfsComponent implements FileSystemX {
    private final AliyunOSSFileSystemProvider fileSystemProvider;
    private final S3FileName root;
    private final OSS oss;

    public AliyunOSSFileSystem(AliyunOSSFileSystemProvider fileSystemProvider,
                               String bucketName,
                               OSS oss) {
        this.fileSystemProvider = fileSystemProvider;
        this.root = new S3FileName(fileSystemProvider.getScheme(), null, null, bucketName, null, "");
        this.oss = oss;
    }

    public OSS getOSS() {
        return oss;
    }

    @Override
    public AliyunOSSFileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }

    @Override
    public AliyunOSSFileObject resolveFile(String name) throws FileSystemException {
        return new AliyunOSSFileObject(this, new S3FileName(root.toURI().resolve(name).toString()));
    }

    @Override
    public FileName getRootName() {
        return root;
    }

    @Override
    public VirtualFileSystem getFileSystemManager() {
        return (VirtualFileSystem) getContext().getFileSystemManager();
    }

    @Override
    public void init() throws FileSystemException {

    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return root.toString();
    }
}
