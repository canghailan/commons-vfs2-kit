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

    @Override
    public S3UriPath getParent() {
        // TODO
        return null;
    }

    @Override
    public S3UriPath resolve(String relative) {
        return new S3UriPath(uri.resolve(relative));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof S3UriPath) {
            S3UriPath that = (S3UriPath) o;
            return Objects.equals(scheme, that.scheme) &&
                    Objects.equals(accessKeyId, that.accessKeyId) &&
                    Objects.equals(secretAccessKey, that.secretAccessKey) &&
                    Objects.equals(bucketName, that.bucketName) &&
                    Objects.equals(endpoint, that.endpoint) &&
                    Objects.equals(key, that.key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, accessKeyId, secretAccessKey, bucketName, endpoint, key);
    }
}
