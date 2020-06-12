package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.File;
import cc.whohow.fs.Provider;
import cc.whohow.fs.UncheckedFileSystemException;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.net.Ping;
import cc.whohow.fs.provider.s3.S3FileResolver;
import cc.whohow.fs.provider.s3.S3Uri;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.Credentials;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyun.oss.model.Bucket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class AliyunOSSFileProvider implements Provider {
    public static final String NAME = "aliyun-oss";
    private static final Logger log = LogManager.getLogger(AliyunOSSFileProvider.class);
    private static final String DEFAULT_ENDPOINT = "oss.aliyuncs.com";
    private final Map<String, Ping> ping = new ConcurrentHashMap<>();
    private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();
    private final Map<String, Credentials> bucketCredentials = new ConcurrentHashMap<>();
    private final Map<S3Uri, OSS> pool = new ConcurrentHashMap<>();
    private final Map<String, AliyunOSSFileSystem> fileSystems = new ConcurrentHashMap<>();
    private volatile String scheme;
    private volatile ClientConfiguration clientConfiguration;
    private volatile List<Credentials> credentialsConfiguration;
    private volatile NavigableMap<String, String> cdnConfiguration;
    private volatile VirtualFileSystem vfs;
    private volatile File<?, ?> context;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception {
        log.debug("initialize AliyunOSSFileProvider: {}", context);

        this.vfs = vfs;
        this.context = context;

        this.scheme = parseScheme();
        this.clientConfiguration = parseClientConfiguration();
        this.credentialsConfiguration = parseProfilesConfiguration();
        this.cdnConfiguration = parseCdnConfiguration();

        log.debug("scan buckets");
        for (Credentials credentials : credentialsConfiguration) {
            OSS oss = new OSSClient(DEFAULT_ENDPOINT, new DefaultCredentialProvider(credentials), clientConfiguration);
            try {
                List<Bucket> bucketList = oss.listBuckets();
                for (Bucket bucket : bucketList) {
                    log.trace("scan bucket: {}", bucket.getName());
                    buckets.put(bucket.getName(), bucket);
                    bucketCredentials.put(bucket.getName(), credentials);
                }
            } finally {
                oss.shutdown();
            }
        }

        log.debug("collect mount points");
        Map<String, S3FileResolver<?, ?>> mount = new TreeMap<>();
        if (isScanEnabled()) {
            log.debug("scan enabled, mount all buckets");
            for (Bucket bucket : buckets.values()) {
                S3Uri s3Uri = new S3Uri(scheme, null, null, bucket.getName(), null, null);
                log.debug("mount bucket {}: ", s3Uri);
                AliyunOSSFileSystem fileSystem = getAliyunOSSFileSystem(s3Uri);
                S3FileResolver<?, ?> fileResolver = new S3FileResolver<>(fileSystem);
                for (String fileSystemUri : fileSystem.getUris()) {
                    mount.put(fileSystemUri, fileResolver);
                }
            }
        }

        log.debug("mount cdn");
        for (Map.Entry<String, String> e : cdnConfiguration.entrySet()) {
            log.debug("mount cdn: {} -> {}", e.getValue(), e.getKey());
            URI uri = URI.create(e.getKey());
            S3Uri s3Uri = new S3Uri(uri);
            AliyunOSSFileSystem fileSystem = getAliyunOSSFileSystem(s3Uri);
            S3FileResolver<?, ?> fileResolver = new S3FileResolver<>(fileSystem, s3Uri.getKey());
            vfs.mount(e.getValue(), fileResolver);
            for (String fileSystemUri : fileSystem.getUris()) {
                mount.put(fileSystemUri + fileResolver.getBase(), fileResolver);
            }
        }

        log.debug("mount vfs");
        for (Map.Entry<String, String> e : vfs.getVfsConfiguration().entrySet()) {
            URI uri = URI.create(e.getValue());
            if (uri.getScheme().equals(scheme)) {
                log.debug("mount vfs: {} -> {}", e.getKey(), e.getValue());
                S3Uri s3Uri = new S3Uri(uri);
                AliyunOSSFileSystem fileSystem = getAliyunOSSFileSystem(s3Uri);
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

    /**
     * 解析scheme
     */
    protected String parseScheme() {
        log.debug("parseScheme");
        return "oss";
    }

    /**
     * 解析客户端配置
     */
    public ClientConfiguration parseClientConfiguration() {
        log.debug("parseClientConfiguration");
        return new ClientConfiguration();
    }

    /**
     * 解析账号配置
     */
    protected List<Credentials> parseProfilesConfiguration() throws IOException {
        log.debug("parseProfilesConfiguration");
        File<?, ?> profilesConfiguration = context.resolve("profiles/");
        try (DirectoryStream<? extends File<?, ?>> profileConfigurations = profilesConfiguration.newDirectoryStream()) {
            List<Credentials> result = new ArrayList<>();
            for (File<?, ?> profileConfiguration : profileConfigurations) {
                String accessKeyId = profileConfiguration.resolve("accessKeyId").readUtf8();
                String secretAccessKey = profileConfiguration.resolve("secretAccessKey").readUtf8();
                result.add(new DefaultCredentials(accessKeyId, secretAccessKey));
            }
            return result;
        }
    }

    /**
     * 解析CDN配置
     */
    protected NavigableMap<String, String> parseCdnConfiguration() throws IOException {
        log.debug("parseCdnConfiguration");
        File<?, ?> cdnsConfiguration = context.resolve("cdn/");
        NavigableMap<String, String> result = new TreeMap<>(Comparator.reverseOrder());
        if (cdnsConfiguration.exists()) {
            try (DirectoryStream<? extends File<?, ?>> cdnConfigurations = cdnsConfiguration.newDirectoryStream()) {
                for (File<?, ?> cdnConfiguration : cdnConfigurations) {
                    String uri = cdnConfiguration.resolve("uri").readUtf8();
                    String cdn = cdnConfiguration.resolve("cdn").readUtf8();
                    result.put(uri, cdn);
                }
                return result;
            }
        } else {
            return result;
        }
    }

    /**
     * 获取OSS文件系统（复用缓存）
     */
    public AliyunOSSFileSystem getAliyunOSSFileSystem(S3Uri uri) {
        log.trace("getAliyunOSSFileSystem: {}", uri);
        return fileSystems.computeIfAbsent(uri.getBucketName(), this::newAliyunOSSFileSystem);
    }

    /**
     * 新创建OSS文件系统（每个bucket一个文件系统）
     */
    protected AliyunOSSFileSystem newAliyunOSSFileSystem(String bucketName) {
        log.debug("newAliyunOSSFileSystem: {}", bucketName);
        Bucket bucket = buckets.get(bucketName);
        if (bucket == null) {
            throw new UncheckedFileSystemException(bucketName + " not exists");
        }
        Credentials credentials = bucketCredentials.get(bucketName);

        String endpoint = getEndpoint(bucket);
        OSS oss = getOSS(new S3Uri(scheme, credentials.getAccessKeyId(), credentials.getSecretAccessKey(), bucket.getName(), endpoint, null));

        S3Uri uri = new S3Uri(scheme, null, null, bucket.getName(), null, "");

        AliyunOSSFileSystemAttributes fileSystemAttributes = new AliyunOSSFileSystemAttributes(NAME);
        fileSystemAttributes.setBucket(bucket);
        fileSystemAttributes.setEndpoint(endpoint);
        fileSystemAttributes.setCnameMap(cdnConfiguration);

        return new AliyunOSSFileSystem(uri, fileSystemAttributes, oss);
    }

    /**
     * 自动探测是否阿里云内网环境，选择Endpoint
     */
    public String getEndpoint(Bucket bucket) {
        log.debug("getEndpoint: {}", bucket.getName());
        if (ping(bucket.getIntranetEndpoint()).getAsLong() >= 0) {
            return bucket.getIntranetEndpoint();
        }
        return bucket.getExtranetEndpoint();
    }

    /**
     * 获取Endpoint ping值
     */
    public Ping ping(String endpoint) {
        log.debug("ping: {}", endpoint);
        return ping.computeIfAbsent(endpoint, Ping::new);
    }

    /**
     * 从连接池中获取OSS客户端
     */
    public OSS getOSS(S3Uri uri) {
        Objects.requireNonNull(uri.getAccessKeyId());
        Objects.requireNonNull(uri.getSecretAccessKey());
        Objects.requireNonNull(uri.getEndpoint());
        log.trace("getOSS: {}", uri.getEndpoint());
        return pool.computeIfAbsent(new S3Uri(null, uri.getAccessKeyId(), uri.getSecretAccessKey(), null, uri.getEndpoint(), null), this::newOSS);
    }

    /**
     * 创建OSS客户端
     */
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
        log.debug("close AliyunOSSFileProvider");
        for (AliyunOSSFileSystem fileSystem : fileSystems.values()) {
            try {
                fileSystem.close();
            } catch (Throwable e) {
                log.warn("close FileSystem error", e);
            }
        }
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
