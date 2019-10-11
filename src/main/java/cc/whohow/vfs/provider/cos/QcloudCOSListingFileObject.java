package cc.whohow.vfs.provider.cos;

import com.qcloud.cos.model.COSObjectSummary;
import org.apache.commons.vfs2.FileSystemException;

public class QcloudCOSListingFileObject extends QcloudCOSFileObject {
    protected final COSObjectSummary objectSummary;

    public QcloudCOSListingFileObject(QcloudCOSFileSystem fileSystem, QcloudCOSFileName name, COSObjectSummary objectSummary) {
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
            case "size": return getSize();
            case "lastModifiedTime": return getLastModifiedTime();
            case "eTag": return objectSummary.getETag();
            default: return super.getAttribute(attrName);
        }
    }
}
