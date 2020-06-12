package cc.whohow.fs.provider.s3;

import cc.whohow.fs.Path;

import java.net.URI;
import java.util.Objects;

public class S3UriPath extends S3Uri implements Path {
    public S3UriPath(URI uri) {
        super(uri);
    }

    public S3UriPath(String uri) {
        super(uri);
    }

    public S3UriPath(String scheme, String bucketName, String key) {
        super(scheme, null, null, bucketName, null, key);
    }

    public boolean isSame(S3UriPath path) {
        return Objects.equals(getKey(), path.getKey()) && Objects.equals(getBucketName(), path.getBucketName());
    }
}
