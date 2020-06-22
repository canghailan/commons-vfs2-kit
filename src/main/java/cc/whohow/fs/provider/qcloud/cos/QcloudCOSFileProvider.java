package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.s3.S3FileResolver;
import cc.whohow.fs.provider.s3.S3Uri;
import cc.whohow.fs.provider.s3.S3UriPath;
import cc.whohow.fs.util.Files;
import cc.whohow.fs.watch.PollingWatchService;
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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;

public class QcloudCOSFileProvider implements FileSystemProvider<S3UriPath, QcloudCOSFile> {
    private static final Logger log = LogManager.getLogger(QcloudCOSFileProvider.class);
    private final Map<String, Bucket> buckets = new ConcurrentSkipListMap<>();
    private final Map<String, COSCredentials> bucketCredentials = new ConcurrentHashMap<>();
    private final Map<S3Uri, COSClient> pool = new ConcurrentHashMap<>();
    private final Map<String, QcloudCOSFileSystem> fileSystems = new ConcurrentHashMap<>();
    private volatile VirtualFileSystem vfs;
    private volatile File<?, ?> metadata;
    private volatile String scheme;
    private volatile boolean automount;
    private volatile ClientConfig clientConfig;
    private volatile List<COSCredentials> credentialsConfiguration;
    private volatile Duration watchInterval;
    private volatile PollingWatchService<S3UriPath, QcloudCOSFile, String> watchService;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> metadata) throws Exception {
        this.vfs = vfs;
        this.metadata = metadata;

        log.debug("initialize QcloudCOSFileProvider: {}", metadata);
        parseConfiguration();
        parseClientConfig();
        parseProfilesConfiguration();
        initializeWatchService();
        scanBuckets();
        mountVfs();
    }

    protected void parseConfiguration() {
        scheme = Files.optional(metadata.resolve("scheme"))
                .map(File::readUtf8)
                .orElse("cos");
        automount = Files.optional(metadata.resolve("automount"))
                .map(File::readUtf8)
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);
        watchInterval = Files.optional(metadata.resolve("watch/interval"))
                .map(File::readUtf8)
                .map(Duration::parse)
                .orElse(Duration.ofSeconds(1));
    }

    /**
     * 解析客户端配置
     */
    public void parseClientConfig() {
        log.debug("parseClientConfig");
        clientConfig = new ClientConfig();
    }

    /**
     * 解析账号配置
     */
    protected void parseProfilesConfiguration() throws IOException {
        log.debug("parseProfilesConfiguration");
        File<?, ?> configurations = metadata.resolve("profiles/");
        try (DirectoryStream<? extends File<?, ?>> stream = configurations.newDirectoryStream()) {
            credentialsConfiguration = new ArrayList<>();
            for (File<?, ?> configuration : stream) {
                String accessKeyId = configuration.resolve("accessKeyId").readUtf8();
                String secretAccessKey = configuration.resolve("secretAccessKey").readUtf8();
                credentialsConfiguration.add(new BasicCOSCredentials(accessKeyId, secretAccessKey));
            }
        }
    }

    protected void initializeWatchService() {
        if (vfs.getScheduledExecutor() != null) {
            watchService = new PollingWatchService<>(vfs.getScheduledExecutor(), watchInterval, QcloudCOSFile::getETag);
        }
    }

    protected void scanBuckets() {
        log.debug("scanBuckets");
        for (COSCredentials credentials : credentialsConfiguration) {
            COSClient cos = new COSClient(credentials, clientConfig);
            try {
                List<Bucket> bucketList = cos.listBuckets();
                for (Bucket bucket : bucketList) {
                    log.trace("scan: {}", bucket.getName());
                    buckets.put(bucket.getName(), bucket);
                    bucketCredentials.put(bucket.getName(), credentials);
                }
            } finally {
                cos.shutdown();
            }
        }
    }

    protected void mountVfs() {
        log.debug("mountVfs");
        log.debug("collect mount points");
        Set<S3UriPath> mountPoints = new HashSet<>();
        if (automount) {
            for (Bucket bucket : buckets.values()) {
                mountPoints.add(new S3UriPath(scheme, bucket.getName(), ""));
            }
        }
        for (String mountPath : vfs.getMountPoints().values()) {
            URI uri = URI.create(mountPath);
            if (scheme.equals(uri.getScheme())) {
                mountPoints.add(new S3UriPath(uri));
            }
        }

        log.debug("build mount points");
        Map<S3UriPath, FileResolver<S3UriPath, QcloudCOSFile>> fileResolvers = new HashMap<>();
        for (S3UriPath mountPoint : mountPoints) {
            QcloudCOSFileSystem fileSystem = getQcloudCOSFileSystem(mountPoint);
            fileResolvers.put(mountPoint, new S3FileResolver<>(fileSystem, mountPoint.getKey()));
        }

        log.debug("mount");
        for (S3UriPath mountPoint : mountPoints) {
            QcloudCOSFileSystem fileSystem = getQcloudCOSFileSystem(mountPoint);
            for (String uri : fileSystem.getUris(mountPoint)) {
                vfs.mount(uri, fileResolvers.get(mountPoint));
            }
        }
        for (Map.Entry<String, String> e : vfs.getMountPoints().entrySet()) {
            URI uri = URI.create(e.getValue());
            if (scheme.equals(uri.getScheme())) {
                vfs.mount(e.getKey(), fileResolvers.get(new S3UriPath(uri)));
            }
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
            throw new UncheckedException(bucketName + " not exists");
        }
        COSCredentials credentials = bucketCredentials.get(bucketName);

        COS cos = getCOS(new S3Uri(scheme, credentials.getCOSAccessKeyId(), credentials.getCOSSecretKey(), bucket.getName(), bucket.getLocation(), null));

        S3Uri uri = new S3Uri(scheme, null, null, bucket.getName(), null, "");

        QcloudCOSFileSystemAttributes fileSystemAttributes = new QcloudCOSFileSystemAttributes();
        fileSystemAttributes.setBucket(bucket);

        return new QcloudCOSFileSystem(this, uri, fileSystemAttributes, cos);
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
        if (watchService != null) {
            try {
                watchService.close();
            } catch (Throwable e) {
                log.debug("close WatchService error", e);
            }
        }
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

    public FileWatchService<S3UriPath, QcloudCOSFile> getWatchService() {
        return watchService;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public FileSystem<S3UriPath, QcloudCOSFile> getFileSystem(URI uri) {
        return getQcloudCOSFileSystem(new S3Uri(uri));
    }

    @Override
    public Collection<? extends FileSystem<S3UriPath, QcloudCOSFile>> getFileSystems() {
        return Collections.unmodifiableCollection(fileSystems.values());
    }

    @Override
    public ExecutorService getExecutor() {
        return vfs.getExecutor();
    }

    @Override
    public CompletableFuture<QcloudCOSFile> copyAsync(QcloudCOSFile source, QcloudCOSFile target) {
        return CompletableFuture.supplyAsync(
                new QcloudCOSCopy(source, target, getExecutor()), getExecutor())
                .join();
    }
}
