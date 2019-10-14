package cc.whohow.vfs.provider.s3;

import cc.whohow.vfs.FileNameImpl;
import cc.whohow.vfs.path.PathBuilder;

import java.net.URI;

public class S3FileName extends S3Uri implements FileNameImpl {
    public S3FileName(String uri) {
        super(uri);
    }

    public S3FileName(URI uri) {
        super(uri);
    }

    public S3FileName(String schema, String accessKeyId, String secretAccessKey, String bucketName, String endpoint, String key) {
        super(schema, accessKeyId, secretAccessKey, bucketName, endpoint, key);
    }

    public S3FileName(S3FileName fileName, String key) {
        super(fileName.getScheme(), fileName.getAccessKeyId(), fileName.getSecretAccessKey(), fileName.getBucketName(), fileName.getEndpoint(), key);
    }

    @Override
    public String getURI() {
        return toString();
    }

    @Override
    public S3FileName getRoot() {
        return new S3FileName(this, "");
    }

    @Override
    public S3FileName getParent() {
        if (key.isEmpty()) {
            return null;
        }
        return new S3FileName(this, new PathBuilder(key).removeLast().endsWithSeparator(true).toString());
    }
}
