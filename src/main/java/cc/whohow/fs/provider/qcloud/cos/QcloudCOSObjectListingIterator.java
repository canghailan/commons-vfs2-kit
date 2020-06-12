package cc.whohow.fs.provider.qcloud.cos;

import com.qcloud.cos.COS;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;

/**
 * COS原始对象遍历器
 */
public class QcloudCOSObjectListingIterator implements Iterator<ObjectListing> {
    private static final Logger log = LogManager.getLogger(QcloudCOSObjectListingIterator.class);
    private final COS cos;
    private final ListObjectsRequest listObjectsRequest;
    private ObjectListing objectListing;

    public QcloudCOSObjectListingIterator(COS cos, String bucketName, String prefix) {
        this(cos, new ListObjectsRequest(bucketName, prefix, null, null, 1000));
    }

    public QcloudCOSObjectListingIterator(COS cos, String bucketName, String prefix, String delimiter) {
        this(cos, new ListObjectsRequest(bucketName, prefix, null, delimiter, 1000));
    }

    public QcloudCOSObjectListingIterator(COS cos, ListObjectsRequest listObjectsRequest) {
        this.cos = cos;
        this.listObjectsRequest = listObjectsRequest;
    }

    public COS getCOS() {
        return cos;
    }

    public String getBucketName() {
        return listObjectsRequest.getBucketName();
    }

    public String getPrefix() {
        return listObjectsRequest.getPrefix();
    }

    @Override
    public boolean hasNext() {
        if (objectListing == null) {
            log.trace("listObjects: cos://{}/{} {}", listObjectsRequest.getBucketName(), listObjectsRequest.getPrefix(), listObjectsRequest.getMarker());
            objectListing = cos.listObjects(listObjectsRequest);
            return true;
        }
        if (objectListing.isTruncated()) {
            log.trace("listNextBatchOfObjects: cos://{}/{} {}", objectListing.getBucketName(), objectListing.getPrefix(), objectListing.getNextMarker());
            objectListing = cos.listNextBatchOfObjects(objectListing);
            return true;
        }
        return false;
    }

    @Override
    public ObjectListing next() {
        return objectListing;
    }
}