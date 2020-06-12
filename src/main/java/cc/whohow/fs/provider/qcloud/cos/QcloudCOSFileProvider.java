package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.File;
import cc.whohow.fs.Provider;
import cc.whohow.fs.UncheckedFileSystemException;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.provider.s3.S3FileResolver;
import cc.whohow.fs.provider.s3.S3Uri;
import com.qcloud.cos.COS;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.region.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class QcloudCOSFileProvider implements Provider {
    public static final String NAME = "qcloud-cos";
    private static final Logger log = LogManager.getLogger(QcloudCOSFileProvider.class);
    private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();
    private final Map<String, COSCredentials> bucketCredentials = new ConcurrentHashMap<>();
    private final Map<S3Uri, COSClient> pool = new ConcurrentHashMap<>();
    private final Map<String, QcloudCOSFileSystem> fileSystems = new ConcurrentHashMap<>();
    private volatile String scheme;
    private volatile ClientConfig clientConfig;
    private volatile List<COSCredentials> credentialsConfiguration;
    private volatile VirtualFileSystem vfs;
    private volatile File<?, ?> context;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception {
        log.debug("initialize QcloudCOSFileProvider: {}", context);

        this.vfs = vfs;
        this.context = context;

        this.scheme = parseScheme();
        this.clientConfig = parseClientConfig();
        this.credentialsConfiguration = parseProfilesConfiguration();

        log.debug("scan buckets");
        for (COSCredentials credentials : credentialsConfiguration) {
            COSClient cos = new COSClient(credentials, clientConfig);
            try {
                List<Bucket> bucketList = cos.listBuckets();
                for (Bucket bucket : bucketList) {
                    log.trace("scan bucket: {}", bucket.getName());
                    buckets.put(bucket.getName(), bucket);
                    bucketCredentials.put(bucket.getName(), credentials);
                }
            } finally {
                cos.shutdown();
            }
        }

        log.debug("collect mount points");
        Map<String, S3FileResolver<?, ?>> mount = new TreeMap<>();
        if (isScanEnabled()) {
            log.debug("scan enabled, mount all buckets");
            for (Bucket bucket : buckets.values()) {
                S3Uri s3Uri = new S3Uri(scheme, null, null, bucket.getName(), null, null);
                log.debug("mount bucket {}: ", s3Uri);
                QcloudCOSFileSystem fileSystem = getQcloudCOSFileSystem(s3Uri);
                S3FileResolver<?, ?> fileResolver = new S3FileResolver<>(fileSystem);
                for (String fileSystemUri : fileSystem.getUris()) {
                    mount.put(fileSystemUri, fileResolver);
                }
            }
        }

        log.debug("mount vfs");
        for (Map.Entry<String, String> e : vfs.getVfsConfiguration().entrySet()) {
            URI uri = URI.create(e.getValue());
            if (uri.getScheme().equals(scheme)) {
                log.debug("mount vfs: {} -> {}", e.getKey(), e.getValue());
                S3Uri s3Uri = new S3Uri(uri);
                QcloudCOSFileSystem fileSystem = getQcloudCOSFileSystem(s3Uri);
                S3FileResolver<?, ?> fileResolver = new S3FileResolver<>(fileSystem, s3Uri.getKey());
                mount.put(e.getKey(), fileResolver);
                for (String fileSystemUri : fileSystem.getUris()) {
                    mount.put(fileSystemUri + fileResolver.getBase(), fileResolver);
                }
            }
        }

        // 合并、优化后挂载到VFS
        for (Map.Entry<String, S3FileResolver<?, ?>> e : mount.entrySet()) {
            vfs.mount(e.getKey(), e.getValue());
        }
    }

    protected boolean isScanEnabled() {
        return false;
    }

    protected String parseScheme() {
        log.debug("parseScheme");
        return "cos";
    }

    /**
     * 解析客户端配置
     */
    public ClientConfig parseClientConfig() {
        log.debug("parseClientConfig");
        return new ClientConfig();
    }

    /**
     * 解析账号配置
     */
    protected List<COSCredentials> parseProfilesConfiguration() throws IOException {
        log.debug("parseProfilesConfiguration");
        File<?, ?> profilesConfiguration = context.resolve("profiles/");
        try (DirectoryStream<? extends File<?, ?>> profileConfigurations = profilesConfiguration.newDirectoryStream()) {
            List<COSCredentials> result = new ArrayList<>();
            for (File<?, ?> profileConfiguration : profileConfigurations) {
                String accessKeyId = profileConfiguration.resolve("accessKeyId").readUtf8();
                String secretAccessKey = profileConfiguration.resolve("secretAccessKey").readUtf8();
                result.add(new BasicCOSCredentials(accessKeyId, secretAccessKey));
            }
            return result;
        }
    }

    /**
     * 获取COS文件系统（复用缓存）
     */
    public QcloudCOSFileSystem getQcloudCOSFileSystem(S3Uri uri) {
        log.trace("getQcloudCOSFileSystem: {}", uri);
        return fileSystems.computeIfAbsent(uri.getBucketName(), this::newQcloudCOSFileSystem);
    }

    /**
     * 新创建COS文件系统（每个bucket一个文件系统）
     */
    protected QcloudCOSFileSystem newQcloudCOSFileSystem(String bucketName) {
        log.debug("newQcloudCOSFileSystem: {}", bucketName);
        Bucket bucket = buckets.get(bucketName);
        if (bucket == null) {
            throw new UncheckedFileSystemException(bucketName + " not exists");
        }
        COSCredentials credentials = bucketCredentials.get(bucketName);

        COS cos = getCOS(new S3Uri(scheme, credentials.getCOSAccessKeyId(), credentials.getCOSSecretKey(), bucket.getName(), bucket.getLocation(), null));

        S3Uri uri = new S3Uri(scheme, null, null, bucket.getName(), null, "");

        QcloudCOSFileSystemAttributes fileSystemAttributes = new QcloudCOSFileSystemAttributes(NAME);
        fileSystemAttributes.setBucket(bucket);

        return new QcloudCOSFileSystem(uri, fileSystemAttributes, cos);
    }

    /**
     * 从连接池中获取COS客户端
     */
    public COSClient getCOS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.trace("getCOS: {}", uri.getEndpoint());
        return pool.computeIfAbsent(new S3Uri(null, uri.getAccessKeyId(), uri.getSecretAccessKey(), null, uri.getEndpoint(), null), this::newCOS);
    }

    /**
     * 创建COS客户端
     */
    protected COSClient newCOS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.debug("newCOS: {}", uri.getEndpoint());
        return new COSClient(new BasicCOSCredentials(uri.getAccessKeyId(), uri.getSecretAccessKey()), new ClientConfig(new Region(uri.getEndpoint())));
    }

    @Override
    public void close() throws Exception {
        log.debug("close QcloudCOSFileProvider");
        for (QcloudCOSFileSystem fileSystem : fileSystems.values()) {
            try {
                fileSystem.close();
            } catch (Throwable e) {
                log.warn("close FileSystem error", e);
            }
        }
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
