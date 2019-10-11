package cc.whohow.vfs.provider.cos;

import com.qcloud.cos.COS;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;

import java.util.Iterator;

public class QcloudCOSObjectListingIterator implements Iterator<ObjectListing> {
    private final COS cos;
    private final ListObjectsRequest listObjectsRequest;
    private ObjectListing objectListing;

    public QcloudCOSObjectListingIterator(COS cos, ListObjectsRequest listObjectsRequest) {
        this.cos = cos;
        this.listObjectsRequest = listObjectsRequest;
    }

    @Override
    public boolean hasNext() {
        if (objectListing == null) {
            objectListing = cos.listObjects(listObjectsRequest);
            return true;
        }
        if (objectListing.isTruncated()) {
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
