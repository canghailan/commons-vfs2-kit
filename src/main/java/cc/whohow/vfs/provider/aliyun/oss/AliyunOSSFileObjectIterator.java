package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.provider.s3.S3FileName;
import cc.whohow.vfs.util.MapIterator;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * VFS 文件遍历器
 * TODO 优化
 */
public class AliyunOSSFileObjectIterator implements Iterator<CloudFileObject> {
    private final AliyunOSSFileObject base;
    private final AliyunOSSObjectListingIterator objectListingIterator;
    private Iterator<CloudFileObject> folders;
    private Iterator<CloudFileObject> files;
    private CloudFileObject file;

    public AliyunOSSFileObjectIterator(AliyunOSSFileObject base, boolean recursively) {
        this.base = base;
        this.objectListingIterator = recursively ?
                new AliyunOSSObjectListingIterator(base.getOSS(), base.getBucketName(), base.getKey()) :
                new AliyunOSSObjectListingIterator(base.getOSS(), base.getBucketName(), base.getKey(), "/");
        this.files = Collections.emptyIterator();
    }

    public AliyunOSSFileObject getBase() {
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
        return new AliyunOSSFileObject(base.getFileSystem(), new S3FileName(base.getName(), key));
    }

    private CloudFileObject newFile(OSSObjectSummary object) {
        return new AliyunOSSListingFileObject(base.getFileSystem(), new S3FileName(base.getName(), object.getKey()), object);
    }
}
