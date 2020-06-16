package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileSystemAttributes;
import cc.whohow.fs.attribute.StringAttribute;
import com.qcloud.cos.model.Bucket;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class QcloudCOSFileSystemAttributes implements FileSystemAttributes {
    public static final String OWNER_ID = "ownerId";
    public static final String BUCKET_NAME = "bucketName";
    public static final String LOCATION = "location";

    private Bucket bucket;

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public String getOwnerId() {
        return bucket.getOwner().getId();
    }

    public String getBucketName() {
        return bucket.getName();
    }

    public String getLocation() {
        return bucket.getLocation();
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        Objects.requireNonNull(name);
        switch (name) {
            case OWNER_ID:
                return Optional.of(new StringAttribute(OWNER_ID, getOwnerId()));
            case BUCKET_NAME:
                return Optional.of(new StringAttribute(BUCKET_NAME, getBucketName()));
            case LOCATION:
                return Optional.of(new StringAttribute(LOCATION, getLocation()));
            default:
                return Optional.empty();
        }
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return Arrays.<Attribute<?>>asList(
                new StringAttribute(OWNER_ID, getOwnerId()),
                new StringAttribute(BUCKET_NAME, getBucketName()),
                new StringAttribute(LOCATION, getLocation())
        ).iterator();
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
