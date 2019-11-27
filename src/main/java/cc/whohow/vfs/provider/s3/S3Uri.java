package cc.whohow.vfs.provider.s3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class S3Uri {
    protected final String scheme;
    protected final String accessKeyId;
    protected final String secretAccessKey;
    protected final String bucketName;
    protected final String endpoint;
    protected final String key;
    protected volatile URI uri;

    public S3Uri(String uri) {
        this(URI.create(uri));
    }

    public S3Uri(URI u) {
        uri = u.normalize();
        if (uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException(uri.toString());
        }
        scheme = uri.getScheme();
        if (uri.getUserInfo() != null) {
            String[] userInfo = uri.getUserInfo().split(":", 2);
            accessKeyId = userInfo[0];
            secretAccessKey = userInfo[1];
        } else {
            accessKeyId = null;
            secretAccessKey = null;
        }
        if (uri.getHost() != null) {
            String[] host = uri.getHost().split("\\.", 2);
            if (host.length == 1) {
                bucketName = host[0];
                endpoint = null;
            } else {
                bucketName = host[0];
                endpoint = host[1];
            }
        } else {
            bucketName = null;
            endpoint = null;
        }
        key = uri.getPath().isEmpty() ? "" : uri.getPath().substring(1);
    }

    public S3Uri(String scheme, String accessKeyId, String secretAccessKey, String bucketName, String endpoint, String key) {
        this.scheme = scheme;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucketName = bucketName;
        this.endpoint = endpoint;
        this.key = key;
    }

    public String getScheme() {
        return scheme;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKey() {
        return key;
    }

    protected String getUserInfo() {
        if (accessKeyId == null || secretAccessKey == null) {
            return null;
        }
        return accessKeyId + ":" + secretAccessKey;
    }

    protected String getHost() {
        if (bucketName == null) {
            return endpoint;
        }
        if (endpoint == null) {
            return bucketName;
        }
        return bucketName + "." + endpoint;
    }

    private String getPath() {
        return "/" + key;
    }

    public URI toURI() {
        if (uri == null) {
            try {
                uri = new URI(scheme, getUserInfo(), getHost(), -1, getPath(), null, null);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        return uri;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, accessKeyId, secretAccessKey, bucketName, endpoint, key);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof S3Uri) {
            S3Uri that = (S3Uri) o;
            return Objects.equals(this.scheme, that.scheme) &&
                    Objects.equals(this.accessKeyId, that.accessKeyId) &&
                    Objects.equals(this.secretAccessKey, that.secretAccessKey) &&
                    Objects.equals(this.bucketName, that.bucketName) &&
                    Objects.equals(this.endpoint, that.endpoint) &&
                    Objects.equals(this.key, that.key);
        }
        return false;
    }

    @Override
    public String toString() {
        return toURI().toString();
    }

    public S3Uri toPublic() {
        if (accessKeyId == null || secretAccessKey == null) {
            return this;
        } else {
            return new S3Uri(scheme, null, null, bucketName, endpoint, key);
        }
    }
}
