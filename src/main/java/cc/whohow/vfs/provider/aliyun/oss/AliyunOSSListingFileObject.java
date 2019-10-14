package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.provider.s3.S3FileName;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.OSSObjectSummary;
import org.apache.commons.vfs2.FileSystemException;


public class AliyunOSSListingFileObject extends AliyunOSSFileObject {
    protected final OSSObjectSummary objectSummary;

    public AliyunOSSListingFileObject(AliyunOSSFileSystem fileSystem, S3FileName name, OSSObjectSummary objectSummary) {
        super(fileSystem, name);
        this.objectSummary = objectSummary;
    }

    @Override
    public long getSize() throws FileSystemException {
        return objectSummary.getSize();
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        return objectSummary.getLastModified().getTime();
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        switch (attrName) {
            case OSSHeaders.CONTENT_LENGTH:
                return objectSummary.getSize();
            case OSSHeaders.LAST_MODIFIED:
                return objectSummary.getLastModified().getTime();
            case OSSHeaders.ETAG:
                return objectSummary.getETag();
            default:
                return super.getAttribute(attrName);
        }
    }
}
