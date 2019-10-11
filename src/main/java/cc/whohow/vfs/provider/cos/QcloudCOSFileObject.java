package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileObjectList;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.ReadableChannelAdapter;
import cc.whohow.vfs.io.WritableChannel;
import com.qcloud.cos.COS;
import com.qcloud.cos.model.ObjectMetadata;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class QcloudCOSFileObject implements CloudFileObject {
    protected final QcloudCOSFileSystem fileSystem;
    protected final QcloudCOSFileName name;

    public QcloudCOSFileObject(QcloudCOSFileSystem fileSystem, QcloudCOSFileName name) {
        this.fileSystem = fileSystem;
        this.name = name;
    }

    public COS getCOS() {
        return fileSystem.getCOS();
    }

    public String getBucketName() {
        return name.getBucketName();
    }

    public String getKey() {
        return name.getKey();
    }

    @Override
    public void createFile() throws FileSystemException {
        // ignore
    }

    @Override
    public void createFolder() throws FileSystemException {
        // ignore
    }

    @Override
    public boolean delete() throws FileSystemException {
        if (isFile()) {
            getCOS().deleteObject(getBucketName(), getKey());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean exists() throws FileSystemException {
        if (isFile()) {
            return getCOS().doesObjectExist(getBucketName(), getKey());
        } else {
            return true;
        }
    }

    @Override
    public QcloudCOSFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public QcloudCOSFileName getName() {
        return name;
    }

    @Override
    public CloudFileObjectList list() throws FileSystemException {
        return new QcloudCOSFileObjectList(this, false);
    }

    @Override
    public CloudFileObjectList listRecursively() throws FileSystemException {
        return new QcloudCOSFileObjectList(this, true);
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        return getCOS().getObject(getBucketName(), getKey()).getObjectContent();
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        if (bAppend) {
            throw new FileSystemException("vfs.provider/write-append-not-supported.error");
        }
        return getWritableChannel();
    }

    @Override
    public ReadableChannel getReadableChannel() throws FileSystemException {
        return new ReadableChannelAdapter(getInputStream());
    }

    @Override
    public WritableChannel getWritableChannel() throws FileSystemException {
        return new QcloudCOSWritableChannel(getCOS(), getBucketName(), getKey());
    }

    protected ObjectMetadata getMetadata() {
        return getCOS().getObjectMetadata(getBucketName(), getKey());
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        return getMetadata().getRawMetadata();
    }

    @Override
    public long getSize() throws FileSystemException {
        return getMetadata().getContentLength();
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        return getMetadata().getLastModified().getTime();
    }

    @Override
    public FileContentInfo getContentInfo() throws FileSystemException {
        ObjectMetadata metadata = getMetadata();
        return new DefaultFileContentInfo(metadata.getContentType(), metadata.getContentEncoding());
    }

    @Override
    public CloudFileObject getParent() throws FileSystemException {
        QcloudCOSFileName parent = getName().getParent();
        if (parent == null) {
            return null;
        }
        return new QcloudCOSFileObject(fileSystem, parent);
    }

    @Override
    public CloudFileObject getChild(String name) throws FileSystemException {
        return new QcloudCOSFileObject(fileSystem, new QcloudCOSFileName(getName(), getKey() + name));
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
