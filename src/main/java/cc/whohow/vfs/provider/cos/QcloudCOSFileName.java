package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.FileNameImpl;
import cc.whohow.vfs.path.PathBuilder;

import java.net.URI;

public class QcloudCOSFileName extends QcloudCOSUri implements FileNameImpl {
    public QcloudCOSFileName(String uri) {
        super(uri);
    }

    public QcloudCOSFileName(URI uri) {
        super(uri);
    }

    public QcloudCOSFileName(String accessKeyId, String secretAccessKey, String bucketName, String endpoint, String key) {
        super(accessKeyId, secretAccessKey, bucketName, endpoint, key);
    }

    public QcloudCOSFileName(QcloudCOSFileName fileName, String key) {
        super(fileName.getAccessKeyId(), fileName.getSecretAccessKey(), fileName.getBucketName(), fileName.getEndpoint(), key);
    }

    @Override
    public String getURI() {
        return toString();
    }

    @Override
    public QcloudCOSFileName getRoot() {
        return new QcloudCOSFileName(this, "");
    }

    @Override
    public QcloudCOSFileName getParent() {
        if (key.isEmpty()) {
            return null;
        }
        return new QcloudCOSFileName(this, new PathBuilder(key).removeLast().endsWithSeparator(true).toString());
    }
}
