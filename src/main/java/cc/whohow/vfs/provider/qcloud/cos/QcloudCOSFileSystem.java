package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.FileSystemProviderX;
import cc.whohow.vfs.FileSystemX;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.provider.s3.S3FileName;
import com.qcloud.cos.COS;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

public class QcloudCOSFileSystem extends AbstractVfsComponent implements FileSystemX {
    protected final QcloudCOSFileSystemProvider fileSystemProvider;
    protected final S3FileName root;
    protected final COS cos;

    public QcloudCOSFileSystem(QcloudCOSFileSystemProvider fileSystemProvider, String bucketName, COS cos) {
        this.fileSystemProvider = fileSystemProvider;
        this.root = new S3FileName(fileSystemProvider.getScheme(), null, null, bucketName, null, "");
        this.cos = cos;
    }

    public COS getCOS() {
        return cos;
    }

    @Override
    public FileSystemProviderX getFileSystemProvider() {
        return fileSystemProvider;
    }

    @Override
    public FileObjectX resolveFile(String name) throws FileSystemException {
        return new QcloudCOSFileObject(this, new S3FileName(root.toURI().resolve(name).toString()));
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
