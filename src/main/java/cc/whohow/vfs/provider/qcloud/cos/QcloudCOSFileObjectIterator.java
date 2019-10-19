package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.provider.s3.S3FileName;
import cc.whohow.vfs.util.MapIterator;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ObjectListing;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;

public class QcloudCOSFileObjectIterator implements Iterator<FileObjectX> {
    private final QcloudCOSFileObject base;
    private final QcloudCOSObjectListingIterator objectListingIterator;
    private Iterator<QcloudCOSFileObject> folders;
    private Iterator<QcloudCOSFileObject> files;
    private QcloudCOSFileObject file;
    private boolean skip = false;

    public QcloudCOSFileObjectIterator(QcloudCOSFileObject base, boolean recursively) {
        this.base = base;
        this.objectListingIterator = recursively ?
                new QcloudCOSObjectListingIterator(base.getCOS(), base.getBucketName(), base.getKey()) :
                new QcloudCOSObjectListingIterator(base.getCOS(), base.getBucketName(), base.getKey(), "/");
        this.files = Collections.emptyIterator();
    }

    public QcloudCOSFileObject getBase() {
        return base;
    }

    @Override
    public boolean hasNext() {
        if (folders != null) {
            file = next(folders);
            if (file != null) {
                return true;
            }
            folders = null;
        }

        file = next(files);
        if (file != null) {
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

    private QcloudCOSFileObject next(Iterator<QcloudCOSFileObject> iterator) {
        if (skip) {
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        }
        if (iterator.hasNext()) {
            QcloudCOSFileObject next = iterator.next();
            if (next.getKey().equals(base.getKey())) {
                skip = true;
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return null;
                }
            } else {
                return next;
            }
        }
        return null;
    }

    @Override
    public FileObjectX next() {
        return file;
    }

    @Override
    public void remove() {
        try {
            file.deleteAll();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private QcloudCOSFileObject newFolder(String key) {
        return new QcloudCOSFileObject(base.getFileSystem(), new S3FileName(base.getName(), key));
    }

    private QcloudCOSFileObject newFile(COSObjectSummary object) {
        return new QcloudCOSListingFileObject(base.getFileSystem(), new S3FileName(base.getName(), object.getKey()), object);
    }
}
