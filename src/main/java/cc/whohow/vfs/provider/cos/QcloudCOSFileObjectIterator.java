package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.util.MapIterator;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;

import java.util.Collections;
import java.util.Iterator;

public class QcloudCOSFileObjectIterator implements Iterator<CloudFileObject> {
    private final QcloudCOSFileObject base;
    private final QcloudCOSObjectListingIterator objectListingIterator;
    private Iterator<CloudFileObject> folders;
    private Iterator<CloudFileObject> files;

    public QcloudCOSFileObjectIterator(QcloudCOSFileObject base, boolean recursively) {
        this.base = base;
        this.objectListingIterator = new QcloudCOSObjectListingIterator(base.getCOS(), new ListObjectsRequest()
                .withBucketName(base.getBucketName())
                .withPrefix(base.getKey())
                .withDelimiter(recursively ? null : "/")
                .withMaxKeys(1));
        this.files = Collections.emptyIterator();
    }

    public QcloudCOSFileObject getBase() {
        return base;
    }

    @Override
    public boolean hasNext() {
        if (folders != null) {
            if (folders.hasNext()) {
                return true;
            } else {
                folders = null;
            }
        }

        if (files.hasNext()) {
            return true;
        }

        if (objectListingIterator.hasNext()) {
            ObjectListing objectListing = objectListingIterator.next();
            folders = new MapIterator<>(objectListing.getCommonPrefixes().iterator(), this::newFolder);
            files = new MapIterator<>(objectListing.getObjectSummaries().iterator(), this::newFile);
            return hasNext();
        }

        return false;
    }

    @Override
    public CloudFileObject next() {
        if (folders != null) {
            return folders.next();
        } else {
            return files.next();
        }
    }

    private CloudFileObject newFolder(String key) {
        QcloudCOSFileName baseName = base.getName();
        return new QcloudCOSFileObject(base.getFileSystem(), new QcloudCOSFileName(baseName, key));
    }

    private CloudFileObject newFile(COSObjectSummary object) {
        return new QcloudCOSListingFileObject(base.getFileSystem(), new QcloudCOSFileName(base.getName(), object.getKey()), object);
    }
}
