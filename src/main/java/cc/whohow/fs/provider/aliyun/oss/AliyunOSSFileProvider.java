package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.FileMetadata;
import cc.whohow.fs.provider.aliyun.cdn.AliyunCDNConfiguration;
import cc.whohow.fs.provider.s3.S3MountPoint;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class AliyunOSSFileProvider implements FileSystemProvider<S3UriPath, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSFileProvider.class);
    private static final String DEFAULT_ENDPOINT = "oss.aliyuncs.com";
    private final Map<String, Ping> ping = new ConcurrentHashMap<>();
    private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();
    private final Map<String, Credentials> bucketCredentials = new ConcurrentHashMap<>();
    private final Map<S3Uri, OSS> pool = new ConcurrentHashMap<>();
    private final Map<String, AliyunOSSFileSystem> fileSystems = new ConcurrentHashMap<>();
    private volatile VirtualFileSystem vfs;
    private volatile FileMetadata metadata;
    private volatile String scheme;
    private volatile boolean automount;
    private volatile ClientConfiguration clientConfiguration;
    private volatile List<Credentials> credentialsConfiguration;
    private volatile List<AliyunCDNConfiguration> cdnConfiguration;
    private volatile Duration watchInterval;
    private volatile PollingWatchService<S3UriPath, AliyunOSSFile, String> watchService;

    @Override
    public void initialize(VirtualFileSystem vfs, File metadata) throws Exception {
        log.debug("initialize AliyunOSSFileProvider: {}", metadata);

        this.vfs = vfs;
        this.metadata = new FileMetadata(metadata);

        readMetadata();
        initializeWatchService();
        scanBuckets();
        mount();
    }

    protected void readMetadata() throws IOException {
        log.debug("readMetadata");

        scheme = this.metadata.getString("scheme")
                .orElse("oss");
        log.debug("scheme: {}", scheme);

        automount = this.metadata.getBoolean("automount")
                .orElse(Boolean.FALSE);
        log.debug("automount: {}", automount);

        watchInterval = this.metadata.getDuration("watch/interval")
                .orElse(Duration.ofSeconds(5));
        log.debug("watch/interval: {}", watchInterval);

        clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSocketTimeout(this.metadata.getInteger("clientConfiguration/socketTimeout")
                .orElse(ClientConfiguration.DEFAULT_SOCKET_TIMEOUT));

        credentialsConfiguration = new ArrayList<>();
        File profilesConfigurations = metadata.getFileMetadata().resolve("profiles/");
        try (DirectoryStream<? extends File> list = profilesConfigurations.newDirectoryStream()) {
            for (File configuration : list) {
                String accessKeyId = configuration.resolve("accessKeyId").readUtf8();
                String secretAccessKey = configuration.resolve("secretAccessKey").readUtf8();
                credentialsConfiguration.add(new DefaultCredentials(accessKeyId, secretAccessKey));
            }
        }
        log.debug("profiles: {}", credentialsConfiguration);

        cdnConfiguration = new ArrayList<>();
        File cdnConfigurations = metadata.getFileMetadata().resolve("cdn/");
        if (cdnConfigurations.exists()) {
            try (DirectoryStream<? extends File> list = cdnConfigurations.newDirectoryStream()) {
                for (File configuration : list) {
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
                                type.orElse("A"), key.get(), ttl.orElse(Duration.ofHours(2))));
                    } else {
                        cdnConfiguration.add(new AliyunCDNConfiguration(origin, cdn));
                    }
                }
            }
        }
        log.debug("cdn: {}", cdnConfiguration);
    }

    protected void initializeWatchService() {
        log.debug("initializeWatchService");
        if (vfs.getScheduledExecutor() != null) {
            watchService = new PollingWatchService<>(vfs.getScheduledExecutor(), watchInterval, AliyunOSSFile::getETag);
        }
    }

    protected void scanBuckets() {
        log.debug("scanBuckets");
        for (Credentials credentials : credentialsConfiguration) {
            OSS oss = new OSSClient(DEFAULT_ENDPOINT, new DefaultCredentialProvider(credentials), clientConfiguration);
            try {
                List<Bucket> bucketList = oss.listBuckets();
                for (Bucket bucket : bucketList) {
                    log.debug("scan: {}", bucket.getName());
                    buckets.put(bucket.getName(), bucket);
                    bucketCredentials.put(bucket.getName(), credentials);
                }
            } finally {
                oss.shutdown();
            }
        }
    }

    protected void mount() {
        log.debug("mount");
        Set<S3UriPath> mountPaths = new HashSet<>();
        if (automount) {
            for (Bucket bucket : buckets.values()) {
                mountPaths.add(new S3UriPath(scheme, bucket.getName(), ""));
            }
        }
        for (String mountPath : vfs.getMetadata().getMountPoints().values()) {
            URI uri = URI.create(mountPath);
            if (scheme.equals(uri.getScheme())) {
                mountPaths.add(new S3UriPath(uri));
            }
        }

        for (S3UriPath mountPath : mountPaths) {
            AliyunOSSFileSystem fileSystem = getAliyunOSSFileSystem(mountPath);
            for (String uri : fileSystem.getUris(mountPath)) {
                vfs.mount(new S3MountPoint<>(uri, fileSystem, mountPath.getKey()));
            }
        }
        for (Map.Entry<String, String> e : vfs.getMetadata().getMountPoints().entrySet()) {
            URI uri = URI.create(e.getValue());
            if (scheme.equals(uri.getScheme())) {
                S3UriPath mountPath = new S3UriPath(uri);
                vfs.mount(new S3MountPoint<>(e.getKey(),
                        getAliyunOSSFileSystem(mountPath), mountPath.getKey()));
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

        AliyunOSSFileSystem fileSystem = new AliyunOSSFileSystem(uri, fileSystemAttributes, oss);
        fileSystem.setWatchService(watchService);
        for (AliyunCDNConfiguration configuration : cdnConfiguration) {
            S3Uri origin = new S3Uri(configuration.getOrigin());
            if (origin.getBucketName().equals(uri.getBucketName())) {
                fileSystem.addCdn(configuration);
            }
        }

        return fileSystem;
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
    public Copy<AliyunOSSFile, AliyunOSSFile> copy(AliyunOSSFile source, AliyunOSSFile target) {
        return new AliyunOSSCopy(source, target);
    }

    @Override
    public void close() throws Exception {
        log.debug("close AliyunOSSFileProvider");
        if (watchService != null) {
            try {
                watchService.close();
            } catch (Throwable e) {
                log.warn("close WatchService error", e);
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
