package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.provider.s3.S3FileName;
import com.qcloud.cos.Headers;
import com.qcloud.cos.model.COSObjectSummary;
import org.apache.commons.vfs2.FileSystemException;

public class QcloudCOSListingFileObject extends QcloudCOSFileObject {
    protected final COSObjectSummary objectSummary;

    public QcloudCOSListingFileObject(QcloudCOSFileSystem fileSystem, S3FileName name, COSObjectSummary objectSummary) {
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
            case Headers.CONTENT_LENGTH:
                return objectSummary.getSize();
            case Headers.LAST_MODIFIED:
                return objectSummary.getLastModified().getTime();
            case Headers.ETAG:
                return objectSummary.getETag();
            default:
                return super.getAttribute(attrName);
        }
    }
}
