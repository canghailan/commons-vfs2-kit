package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.provider.s3.S3UriPath;
import cc.whohow.fs.util.IteratorIterator;
import cc.whohow.fs.util.MappingIterator;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ObjectListing;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * 文件遍历器
 */
public class QcloudCOSFileIterator implements Iterator<QcloudCOSFile> {
    private final QcloudCOSFileSystem fileSystem;
    private final S3UriPath path;
    private final QcloudCOSObjectListingIterator objectListingIterator;
    private Iterator<QcloudCOSFile> iterator;
    private QcloudCOSFile file;

    public QcloudCOSFileIterator(QcloudCOSFileSystem fileSystem, S3UriPath path, boolean recursively) {
        this.fileSystem = fileSystem;
        this.path = path;
        this.objectListingIterator = recursively ?
                new QcloudCOSObjectListingIterator(fileSystem.getCOS(), path.getBucketName(), path.getKey()) :
                new QcloudCOSObjectListingIterator(fileSystem.getCOS(), path.getBucketName(), path.getKey(), "/");
        this.iterator = Collections.emptyIterator();
    }

    public S3UriPath getPath() {
        return path;
    }

    @Override
    public boolean hasNext() {
        if (iterator.hasNext()) {
            return true;
        }
        while (objectListingIterator.hasNext()) {
            ObjectListing objectListing = objectListingIterator.next();
            if (objectListing.getCommonPrefixes().isEmpty()) {
                iterator = new MappingIterator<>(objectListing.getObjectSummaries().iterator(), this::newFile);
            } else {
                if (objectListing.getObjectSummaries().isEmpty()) {
                    iterator = new MappingIterator<>(objectListing.getCommonPrefixes().iterator(), this::newDirectory);
                } else {
                    iterator = new IteratorIterator<>(Arrays.asList(
                            new MappingIterator<>(objectListing.getCommonPrefixes().iterator(), this::newDirectory),
                            new MappingIterator<>(objectListing.getObjectSummaries().iterator(), this::newFile)
                    ));
                }
            }
            if (iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public QcloudCOSFile next() {
        return file = iterator.next();
    }

    @Override
    public void remove() {
        file.delete();
    }

    private QcloudCOSFile newDirectory(String key) {
        return fileSystem.get(fileSystem.resolve(key));
    }

    private QcloudCOSFile newFile(COSObjectSummary objectSummary) {
        return new QcloudCOSStatFile(
                fileSystem, fileSystem.resolve(objectSummary.getKey()),
                new QcloudCOSObjectSummaryFileAttributes(objectSummary));
    }
}
