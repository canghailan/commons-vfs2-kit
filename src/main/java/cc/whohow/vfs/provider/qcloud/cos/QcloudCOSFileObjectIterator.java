package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.provider.s3.S3FileName;
import cc.whohow.vfs.util.MapIterator;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;

public class QcloudCOSFileObjectIterator implements Iterator<CloudFileObject> {
    private final QcloudCOSFileObject base;
    private final QcloudCOSObjectListingIterator objectListingIterator;
    private Iterator<CloudFileObject> folders;
    private Iterator<CloudFileObject> files;
    private CloudFileObject file;

    public QcloudCOSFileObjectIterator(QcloudCOSFileObject base, boolean recursively) {
        this.base = base;
        this.objectListingIterator = new QcloudCOSObjectListingIterator(base.getCOS(), new ListObjectsRequest(
                base.getBucketName(),
                base.getKey(),
                null,
                recursively ? null : "/",
                1000));
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
            return file = folders.next();
        } else {
            return file = files.next();
        }
    }

    @Override
    public void remove() {
        try {
            file.deleteAll();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CloudFileObject newFolder(String key) {
        return new QcloudCOSFileObject(base.getFileSystem(), new S3FileName(base.getName(), key));
    }

    private CloudFileObject newFile(COSObjectSummary object) {
        return new QcloudCOSListingFileObject(base.getFileSystem(), new S3FileName(base.getName(), object.getKey()), object);
    }
}
