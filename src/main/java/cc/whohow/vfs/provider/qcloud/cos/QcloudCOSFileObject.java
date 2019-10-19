package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.ReadableChannelAdapter;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.provider.s3.S3FileAttributes;
import cc.whohow.vfs.provider.s3.S3FileName;
import com.qcloud.cos.COS;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QcloudCOSFileObject implements FileObjectX {
    protected final QcloudCOSFileSystem fileSystem;
    protected final S3FileName name;

    public QcloudCOSFileObject(QcloudCOSFileSystem fileSystem, S3FileName name) {
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
    public S3FileName getName() {
        return name;
    }

    @Override
    public DirectoryStream<FileObjectX> list() throws FileSystemException {
        return new QcloudCOSFileObjectList(this, false);
    }

    @Override
    public DirectoryStream<FileObjectX> listRecursively() throws FileSystemException {
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

    protected ObjectMetadata getObjectMetadata() {
        return getCOS().getObjectMetadata(getBucketName(), getKey());
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        ObjectMetadata objectMetadata = getObjectMetadata();
        return new S3FileAttributes(objectMetadata.getRawMetadata(), objectMetadata.getUserMetadata());
    }

    @Override
    public long getSize() throws FileSystemException {
        return getObjectMetadata().getContentLength();
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        return getObjectMetadata().getLastModified().getTime();
    }

    @Override
    public FileContentInfo getContentInfo() throws FileSystemException {
        ObjectMetadata metadata = getObjectMetadata();
        return new DefaultFileContentInfo(metadata.getContentType(), metadata.getContentEncoding());
    }

    @Override
    public QcloudCOSFileObject getParent() throws FileSystemException {
        S3FileName parent = getName().getParent();
        if (parent == null) {
            return null;
        }
        return new QcloudCOSFileObject(fileSystem, parent);
    }

    @Override
    public QcloudCOSFileObject getChild(String name) throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return new QcloudCOSFileObject(fileSystem, new S3FileName(getName(), getKey() + name));
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public int deleteAll() throws FileSystemException {
        if (isFile()) {
            getCOS().deleteObject(getBucketName(), getKey());
            return 1;
        } else {
            int n = 0;
            QcloudCOSObjectListingIterator iterator = new QcloudCOSObjectListingIterator(getCOS(), getBucketName(), getKey());
            while (iterator.hasNext()) {
                ObjectListing objectListing = iterator.next();
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    List<DeleteObjectsRequest.KeyVersion> keys = objectListing.getObjectSummaries().stream()
                            .map(COSObjectSummary::getKey)
                            .map(DeleteObjectsRequest.KeyVersion::new)
                            .collect(Collectors.toList());
                    getCOS().deleteObjects(new DeleteObjectsRequest(getBucketName()).withKeys(keys));
                }
                n += objectListing.getCommonPrefixes().size();
                n += objectListing.getObjectSummaries().size();
            }
            return n;
        }
    }
}
