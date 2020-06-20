package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.provider.s3.S3UriPath;
import cc.whohow.fs.util.IteratorIterator;
import cc.whohow.fs.util.MappingIterator;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * 文件遍历器
 */
public class AliyunOSSFileIterator implements Iterator<AliyunOSSFile> {
    private final AliyunOSSFileSystem fileSystem;
    private final S3UriPath path;
    private final AliyunOSSObjectListingIterator objectListingIterator;
    private Iterator<AliyunOSSFile> iterator;
    private AliyunOSSFile file;

    public AliyunOSSFileIterator(AliyunOSSFileSystem fileSystem, S3UriPath path, boolean recursively) {
        this.fileSystem = fileSystem;
        this.path = path;
        this.objectListingIterator = recursively ?
                new AliyunOSSObjectListingIterator(fileSystem.getOSS(), path.getBucketName(), path.getKey()) :
                new AliyunOSSObjectListingIterator(fileSystem.getOSS(), path.getBucketName(), path.getKey(), "/");
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
    public AliyunOSSFile next() {
        return file = iterator.next();
    }

    @Override
    public void remove() {
        file.delete();
    }

    private AliyunOSSFile newDirectory(String key) {
        return new AliyunOSSFile(
                fileSystem,
                new S3UriPath(path.getScheme(), path.getBucketName(), key));
    }

    private AliyunOSSFile newFile(OSSObjectSummary objectSummary) {
        return new AliyunOSSStatFile(
                fileSystem,
                new S3UriPath(path.getScheme(), path.getBucketName(), objectSummary.getKey()),
                new AliyunOSSObjectSummaryFileAttributes(objectSummary));
    }
}
