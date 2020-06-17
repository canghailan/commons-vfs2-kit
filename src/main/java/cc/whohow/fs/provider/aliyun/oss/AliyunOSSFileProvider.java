package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.aliyun.cdn.AliyunCDNConfiguration;
import cc.whohow.fs.provider.s3.S3FileResolver;
import cc.whohow.fs.provider.s3.S3Uri;
import cc.whohow.fs.provider.s3.S3UriPath;
import cc.whohow.fs.util.Files;
import cc.whohow.fs.util.Ping;
import cc.whohow.fs.watch.PollingWatchService;
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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;

public class AliyunOSSFileProvider implements FileSystemProvider<S3UriPath, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSFileProvider.class);
    private static final String DEFAULT_ENDPOINT = "oss.aliyuncs.com";
    private final Map<String, Ping> ping = new ConcurrentHashMap<>();
    private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();
    private final Map<String, Credentials> bucketCredentials = new ConcurrentHashMap<>();
    private final Map<S3Uri, OSS> pool = new ConcurrentHashMap<>();
    private final Map<String, AliyunOSSFileSystem> fileSystems = new ConcurrentHashMap<>();
    private volatile PollingWatchService<S3UriPath, AliyunOSSFile, String> watchService;
    private volatile VirtualFileSystem vfs;
    private volatile File<?, ?> context;
    private volatile String scheme;
    private volatile boolean automount;
    private volatile ClientConfiguration clientConfiguration;
    private volatile List<Credentials> credentialsConfiguration;
    private volatile List<AliyunCDNConfiguration> cdnConfiguration;
    private volatile Duration watchInterval;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception {
        this.vfs = vfs;
        this.context = context;
        log.debug("initialize AliyunOSSFileProvider: {}", context);

        parseConfiguration();
        parseClientConfiguration();
        parseProfilesConfiguration();
        parseCdnConfiguration();

        if (vfs.getScheduledExecutor() != null) {
            watchService = new PollingWatchService<>(vfs.getScheduledExecutor(), watchInterval, AliyunOSSFile::getETag);
        }

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
        if (automount) {
            log.debug("automount enabled, mount all buckets");
            for (Bucket bucket : buckets.values()) {
                S3Uri s3Uri = new S3Uri(scheme, null, null, bucket.getName(), null, "");
                log.debug("mount bucket {}: ", s3Uri);
                AliyunOSSFileSystem fileSystem = getAliyunOSSFileSystem(s3Uri);
                S3FileResolver<?, ?> fileResolver = new S3FileResolver<>(fileSystem);
                for (String fileSystemUri : fileSystem.getUris()) {
                    mount.put(fileSystemUri, fileResolver);
                }
            }
        }

        log.debug("mount cdn");
        for (AliyunCDNConfiguration cdnConf : cdnConfiguration) {
            log.debug("mount cdn: {} -> {}", cdnConf.getCdn(), cdnConf.getOrigin());
            URI uri = URI.create(cdnConf.getOrigin());
            S3Uri s3Uri = new S3Uri(uri);
            AliyunOSSFileSystem fileSystem = getAliyunOSSFileSystem(s3Uri);
            S3FileResolver<?, ?> fileResolver = new S3FileResolver<>(fileSystem, s3Uri.getKey());
            vfs.mount(cdnConf.getOrigin(), fileResolver);
            for (String fileSystemUri : fileSystem.getUris()) {
                mount.put(fileSystemUri + fileResolver.getBase(), fileResolver);
            }
        }

        log.debug("mount vfs");
        for (Map.Entry<String, String> e : vfs.getMountPoints().entrySet()) {
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

    /**
     * 解析配置
     */
    protected void parseConfiguration() {
        scheme = Files.optional(context.resolve("scheme"))
                .map(File::readUtf8)
                .orElse("oss");
        automount = Files.optional(context.resolve("automount"))
                .map(File::readUtf8)
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);
        watchInterval = Files.optional(context.resolve("watch/interval"))
                .map(File::readUtf8)
                .map(Duration::parse)
                .orElse(Duration.ofSeconds(1));
    }

    /**
     * 解析客户端配置
     */
    public void parseClientConfiguration() {
        log.debug("parseClientConfiguration");
        clientConfiguration = new ClientConfiguration();
    }

    /**
     * 解析账号配置
     */
    protected void parseProfilesConfiguration() throws IOException {
        log.debug("parseProfilesConfiguration");
        credentialsConfiguration = new ArrayList<>();
        File<?, ?> configurations = context.resolve("profiles/");
        try (DirectoryStream<? extends File<?, ?>> stream = configurations.newDirectoryStream()) {
            for (File<?, ?> configuration : stream) {
                String accessKeyId = configuration.resolve("accessKeyId").readUtf8();
                String secretAccessKey = configuration.resolve("secretAccessKey").readUtf8();
                credentialsConfiguration.add(new DefaultCredentials(accessKeyId, secretAccessKey));
            }
        }
    }

    /**
     * 解析CDN配置
     */
    protected void parseCdnConfiguration() throws IOException {
        log.debug("parseCdnConfiguration");
        cdnConfiguration = new ArrayList<>();
        File<?, ?> configurations = context.resolve("cdn/");
        if (configurations.exists()) {
            try (DirectoryStream<? extends File<?, ?>> stream = configurations.newDirectoryStream()) {
                for (File<?, ?> configuration : stream) {
                    String origin = configuration.resolve("origin").readUtf8();
                    String cdn = configuration.resolve("cdn").readUtf8();
                    Optional<String> type = Files.optional(configuration.resolve("type"))
                            .map(File::readUtf8);
                    Optional<String> key = Files.optional(configuration.resolve("key"))
                            .map(File::readUtf8);
                    Optional<Duration> ttl = Files.optional(configuration.resolve("ttl"))
                            .map(File::readUtf8)
                            .map(Duration::parse);
                    if (key.isPresent()) {
                        cdnConfiguration.add(new AliyunCDNConfiguration(origin, cdn,
                                type.orElse("A"), key.get(), ttl.orElse(Duration.ofMinutes(120))));
                    } else {
                        cdnConfiguration.add(new AliyunCDNConfiguration(origin, cdn));
                    }
                }
            }
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
            throw new IllegalArgumentException(bucketName + " not exists");
        }
        Credentials credentials = bucketCredentials.get(bucketName);

        String endpoint = getEndpoint(bucket);
        OSS oss = getOSS(new S3Uri(scheme, credentials.getAccessKeyId(), credentials.getSecretAccessKey(), bucket.getName(), endpoint, null));

        S3Uri uri = new S3Uri(scheme, null, null, bucket.getName(), null, "");

        AliyunOSSFileSystemAttributes fileSystemAttributes = new AliyunOSSFileSystemAttributes();
        fileSystemAttributes.setBucket(bucket);
        fileSystemAttributes.setEndpoint(endpoint);
        fileSystemAttributes.setCdnConfiguration(cdnConfiguration);

        return new AliyunOSSFileSystem(this, uri, fileSystemAttributes, oss);
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

    public FileWatchService<S3UriPath, AliyunOSSFile> getWatchService() {
        return watchService;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public FileSystem<S3UriPath, AliyunOSSFile> getFileSystem(URI uri) {
        return getAliyunOSSFileSystem(new S3Uri(uri));
    }

    @Override
    public Collection<? extends FileSystem<S3UriPath, AliyunOSSFile>> getFileSystems() {
        return Collections.unmodifiableCollection(fileSystems.values());
    }

    @Override
    public ExecutorService getExecutor() {
        return vfs.getExecutor();
    }

    @Override
    public CompletableFuture<AliyunOSSFile> copyAsync(AliyunOSSFile source, AliyunOSSFile target) {
        return CompletableFuture.supplyAsync(new AliyunOSSCopy(source, target).withExecutor(getExecutor()), getExecutor());
    }

    @Override
    public void close() throws Exception {
        log.debug("close AliyunOSSFileProvider");
        if (watchService != null) {
            try {
                watchService.close();
            } catch (Throwable e) {
                log.debug("close WatchService error", e);
            }
        }
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
