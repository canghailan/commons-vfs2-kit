package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileSystemAttributes;
import cc.whohow.fs.attribute.StringAttribute;
import cc.whohow.fs.provider.aliyun.cdn.AliyunCDNConfiguration;
import com.aliyun.oss.model.Bucket;

import java.util.*;

public class AliyunOSSFileSystemAttributes implements FileSystemAttributes {
    public static final String ENDPOINT = "endpoint";
    public static final String OWNER_ID = "ownerId";
    public static final String BUCKET_NAME = "bucketName";
    public static final String LOCATION = "location";
    public static final String STORAGE_CLASS = "storageClass";
    public static final String EXTRANET_ENDPOINT = "extranetEndpoint";
    public static final String INTRANET_ENDPOINT = "intranetEndpoint";

    private final String provider;
    private Bucket bucket;
    private String endpoint;
    private List<AliyunCDNConfiguration> cdnConfiguration;

    public AliyunOSSFileSystemAttributes(String provider) {
        this.provider = provider;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    @Override
    public String provider() {
        return provider;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public String getStorageClass() {
        return bucket.getStorageClass().toString();
    }

    public String getExtranetEndpoint() {
        return bucket.getExtranetEndpoint();
    }

    public String getIntranetEndpoint() {
        return bucket.getIntranetEndpoint();
    }

    public List<AliyunCDNConfiguration> getCdnConfiguration() {
        return cdnConfiguration;
    }

    public void setCdnConfiguration(List<AliyunCDNConfiguration> cdnConfiguration) {
        this.cdnConfiguration = cdnConfiguration;
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        Objects.requireNonNull(name);
        switch (name) {
            case PROVIDER:
                return Optional.of(new StringAttribute(PROVIDER, provider()));
            case OWNER_ID:
                return Optional.of(new StringAttribute(OWNER_ID, getOwnerId()));
            case ENDPOINT:
                return Optional.of(new StringAttribute(ENDPOINT, getEndpoint()));
            case BUCKET_NAME:
                return Optional.of(new StringAttribute(BUCKET_NAME, getBucketName()));
            case LOCATION:
                return Optional.of(new StringAttribute(LOCATION, getLocation()));
            case STORAGE_CLASS:
                return Optional.of(new StringAttribute(STORAGE_CLASS, getStorageClass()));
            case EXTRANET_ENDPOINT:
                return Optional.of(new StringAttribute(EXTRANET_ENDPOINT, getExtranetEndpoint()));
            case INTRANET_ENDPOINT:
                return Optional.of(new StringAttribute(INTRANET_ENDPOINT, getIntranetEndpoint()));
            default:
                return Optional.empty();
        }
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return Arrays.<Attribute<?>>asList(
                new StringAttribute(PROVIDER, provider()),
                new StringAttribute(OWNER_ID, getOwnerId()),
                new StringAttribute(ENDPOINT, getEndpoint()),
                new StringAttribute(BUCKET_NAME, getBucketName()),
                new StringAttribute(LOCATION, getLocation()),
                new StringAttribute(STORAGE_CLASS, getStorageClass()),
                new StringAttribute(EXTRANET_ENDPOINT, getExtranetEndpoint()),
                new StringAttribute(INTRANET_ENDPOINT, getIntranetEndpoint())
        ).iterator();
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
