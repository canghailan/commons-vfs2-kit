package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.ObjectFile;
import cc.whohow.fs.ObjectFileManager;
import cc.whohow.fs.provider.s3.S3Uri;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.region.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class QcloudCOSObjectManager implements ObjectFileManager {
    private static final Logger log = LogManager.getLogger(QcloudCOSObjectManager.class);
    private final Map<S3Uri, COSClient> pool = new ConcurrentHashMap<>();
    private final ClientConfig clientConfig;

    public QcloudCOSObjectManager() {
        this(new ClientConfig());
    }

    public QcloudCOSObjectManager(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public String getScheme() {
        return "cos";
    }

    @Override
    public ObjectFile get(CharSequence uri) {
        S3Uri s3Uri = new S3Uri(uri.toString());
        return new QcloudCOSObjectFile(getCOS(s3Uri), new S3Uri(s3Uri.getScheme(), null, null, s3Uri.getBucketName(), null, s3Uri.getKey()));
    }

    public COSClient getCOS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.trace("getCOS: {}", uri.getEndpoint());
        return pool.computeIfAbsent(new S3Uri(null, uri.getAccessKeyId(), uri.getSecretAccessKey(), null, null, null), this::newCOS);
    }

    protected COSClient newCOS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.debug("newCOS: {}", uri.getEndpoint());
        return new COSClient(new BasicCOSCredentials(uri.getAccessKeyId(), uri.getSecretAccessKey()), new ClientConfig(new Region(uri.getEndpoint())));
    }

    @Override
    public void close() throws Exception {
        log.debug("close QcloudCOSObjectFactory");
        for (COSClient cos : pool.values()) {
            try {
                log.debug("shutdown COS: {}", cos);
                cos.shutdown();
            } catch (Throwable e) {
                log.warn("shutdown COS error", e);
            }
        }
    }
}
