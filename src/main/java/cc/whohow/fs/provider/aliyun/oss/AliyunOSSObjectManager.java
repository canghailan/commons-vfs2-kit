package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.ObjectFile;
import cc.whohow.fs.ObjectFileManager;
import cc.whohow.vfs.provider.s3.S3Uri;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AliyunOSSObjectManager implements ObjectFileManager {
    private static final Logger log = LogManager.getLogger(AliyunOSSObjectManager.class);
    private final Map<S3Uri, OSS> pool = new ConcurrentHashMap<>();
    private final ClientConfiguration clientConfiguration;

    public AliyunOSSObjectManager() {
        this(new ClientConfiguration());
    }

    public AliyunOSSObjectManager(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    @Override
    public ObjectFile get(CharSequence uri) {
        S3Uri s3Uri = new S3Uri(uri.toString());
        return new AliyunOSSObjectFile(getOSS(s3Uri), s3Uri);
    }

    public OSS getOSS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.trace("getOSS: {}", uri.getEndpoint());
        return pool.computeIfAbsent(new S3Uri(null, uri.getAccessKeyId(), uri.getSecretAccessKey(), null, uri.getEndpoint(), null), this::newOSS);
    }

    protected OSS newOSS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.debug("newOSS: {}", uri.getEndpoint());
        return new OSSClient(
                uri.getEndpoint(),
                new DefaultCredentialProvider(new DefaultCredentials(uri.getAccessKeyId(), uri.getSecretAccessKey())),
                clientConfiguration);
    }

    @Override
    public void close() throws Exception {
        log.debug("close AliyunOSSObjectFactory");
        for (OSS oss : pool.values()) {
            try {
                log.debug("shutdown OSS: {}", oss);
                oss.shutdown();
            } catch (Throwable e) {
                log.warn("shutdown OSS error", e);
            }
        }
    }
}
