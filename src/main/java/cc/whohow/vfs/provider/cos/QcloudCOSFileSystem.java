package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.*;
import com.qcloud.cos.COS;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

public class QcloudCOSFileSystem extends AbstractVfsComponent implements CloudFileSystem {
    protected final QcloudCOSFileSystemProvider fileSystemProvider;
    protected final QcloudCOSFileName root;
    protected final COS cos;

    public QcloudCOSFileSystem(QcloudCOSFileSystemProvider fileSystemProvider, String bucketName, COS cos) {
        this.fileSystemProvider = fileSystemProvider;
        this.root = new QcloudCOSFileName(null, null, bucketName, null, "");
        this.cos = cos;
    }

    public COS getCOS() {
        return cos;
    }

    @Override
    public CloudFileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }

    @Override
    public CloudFileObject resolve(CharSequence name) throws FileSystemException {
        return new QcloudCOSFileObject(this, new QcloudCOSFileName(root.toURI().resolve(name.toString()).toString()));
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

    public QcloudCOSFileName getFileName(String key) {
        return new QcloudCOSFileName(root.getAccessKeyId(), root.getSecretAccessKey(), root.getBucketName(), root.getEndpoint(), key);
    }
}
