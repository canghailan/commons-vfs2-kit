package cc.whohow.fs.provider.s3;

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
        if (uri.getUserInfo() == null) {
            accessKeyId = null;
            secretAccessKey = null;
        } else {
            int index = uri.getUserInfo().indexOf(':');
            if (index < 0) {
                throw new IllegalArgumentException(uri.toString());
            }
            accessKeyId = uri.getUserInfo().substring(0, index);
            secretAccessKey = uri.getUserInfo().substring(index + 1);
        }
        if (uri.getHost() == null) {
            bucketName = null;
            endpoint = null;
        } else {
            int index = uri.getHost().indexOf('.');
            if (index < 0) {
                bucketName = uri.getHost();
                endpoint = null;
            } else {
                bucketName = uri.getHost().substring(0, index);
                endpoint = uri.getHost().substring(index + 1);
            }
        }
        if (uri.getPath() == null) {
            key = null;
        } else {
            if (uri.getPath().length() <= 1) {
                key = "";
            } else {
                key = uri.getPath().substring(1);
            }
        }
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

    protected String getPath() {
        if (key == null) {
            return null;
        }
        return "/" + key;
    }

    public URI toUri() {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof S3Uri) {
            S3Uri that = (S3Uri) o;
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

    @Override
    public String toString() {
        return toUri().toString();
    }
}
