package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.ReadableChannelAdapter;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.provider.s3.S3FileAttributes;
import cc.whohow.vfs.provider.s3.S3FileName;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.vfs2.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * 阿里云文件对象
 */
public class AliyunOSSFileObject implements CloudFileObject {
    protected final AliyunOSSFileSystem fileSystem;
    protected final S3FileName name;

    public AliyunOSSFileObject(AliyunOSSFileSystem fileSystem, S3FileName name) {
        this.fileSystem = fileSystem;
        this.name = name;
    }

    public OSS getOSS() {
        return fileSystem.getOSS();
    }

    public String getBucketName() {
        return name.getBucketName();
    }

    public String getKey() {
        return name.getKey();
    }

    /**
     * 创建文件，无需任何操作
     */
    public void createFile() {
        // do nothing
    }

    /**
     * 创建目录，无需任何操作
     */
    public void createFolder() {
        // do nothing
    }

    @Override
    public boolean delete() throws FileSystemException {
        if (isFile()) {
            getOSS().deleteObject(getBucketName(), getKey());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 文件是否存在
     */
    public boolean exists() throws FileSystemException {
        if (isFile()) {
            return getOSS().doesObjectExist(getBucketName(), getKey());
        } else {
            return true;
        }
    }

    @Override
    public AliyunOSSFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public S3FileName getName() {
        return name;
    }

    @Override
    public DirectoryStream<CloudFileObject> list() throws FileSystemException {
        return new AliyunOSSFileObjectList(this, false);
    }

    @Override
    public DirectoryStream<CloudFileObject> listRecursively() throws FileSystemException {
        return new AliyunOSSFileObjectList(this, true);
    }

    protected ObjectMetadata getObjectMetadata() {
        return getOSS().getObjectMetadata(getBucketName(), getKey());
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        ObjectMetadata objectMetadata = getObjectMetadata();
        return new S3FileAttributes(objectMetadata.getRawMetadata(), objectMetadata.getUserMetadata());
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        return getOSS().getObject(getBucketName(), getKey()).getObjectContent();
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        if (bAppend) {
            return new AliyunOSSWritableChannel(getOSS(), getBucketName(), getKey(), getSize());
        } else {
            return new AliyunOSSWritableChannel(getOSS(), getBucketName(), getKey());
        }
    }

    @Override
    public ReadableChannel getReadableChannel() throws FileSystemException {
        return new ReadableChannelAdapter(getInputStream());
    }

    @Override
    public WritableChannel getWritableChannel() throws FileSystemException {
        return new AliyunOSSWritableChannel(getOSS(), getBucketName(), getKey());
    }

    /**
     * 获取上级目录
     */
    public AliyunOSSFileObject getParent() {
        S3FileName parent = getName().getParent();
        if (parent == null) {
            return null;
        }
        return new AliyunOSSFileObject(getFileSystem(), parent);
    }

    /**
     * 获取下级文件
     */
    public AliyunOSSFileObject getChild(String name) throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return new AliyunOSSFileObject(getFileSystem(), new S3FileName(getName(), getKey() + name));
    }

    /**
     * 获取URL
     */
    public URL getURL() {
        try {
            return new URL(fileSystem.getFileSystemProvider().getURL(getName(), new Date(System.currentTimeMillis() + Duration.ofDays(1).toMillis())));
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
