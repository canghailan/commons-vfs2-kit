package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.aliyun.cdn.AliyunCDNConfiguration;
import cc.whohow.fs.provider.s3.S3Uri;
import cc.whohow.fs.provider.s3.S3UriPath;
import cc.whohow.fs.util.FileTree;
import cc.whohow.fs.util.Files;
import cc.whohow.fs.util.FilterIterable;
import cc.whohow.fs.util.MappingPredicate;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.stream.Collectors;

public class AliyunOSSFileSystem implements FileSystem<S3UriPath, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSFileSystem.class);
    protected final S3Uri uri;
    protected final AliyunOSSFileSystemAttributes attributes;
    protected final OSS oss;
    // key -> [cdn]
    protected final Map<String, List<AliyunCDNConfiguration>> cdnIndex = new LinkedHashMap<>();
    protected volatile FileWatchService<S3UriPath, AliyunOSSFile> watchService;

    public AliyunOSSFileSystem(S3Uri uri, AliyunOSSFileSystemAttributes attributes, OSS oss) {
        if (uri.getScheme() == null ||
                uri.getAccessKeyId() != null ||
                uri.getSecretAccessKey() != null ||
                uri.getBucketName() == null ||
                uri.getEndpoint() != null ||
                uri.getKey() == null) {
            throw new IllegalArgumentException(uri.toString());
        }
        this.uri = uri;
        this.attributes = attributes;
        this.oss = oss;
    }

    public void addCdn(AliyunCDNConfiguration configuration) {
        S3Uri origin = new S3Uri(URI.create(configuration.getOrigin()));
        if (!origin.getBucketName().equals(uri.getBucketName())) {
            throw new IllegalArgumentException(configuration.getOrigin());
        }
        cdnIndex.computeIfAbsent(origin.getKey(), key -> new ArrayList<>())
                .add(configuration);
    }

    public OSS getOSS() {
        return oss;
    }

    @Override
    public URI getUri() {
        return uri.toUri();
    }

    @Override
    public String getPublicUri(S3UriPath path) {
        for (Map.Entry<String, List<AliyunCDNConfiguration>> e : cdnIndex.entrySet()) {
            if (path.getKey().startsWith(e.getKey())) {
                AliyunCDNConfiguration cdn = e.getValue().get(0);
                return cdn.getCdn() + path.getKey().substring(e.getKey().length());
            }
        }
        return getExtranetUri(path);
    }

    public String getExtranetUri(S3UriPath path) {
        return "https://" + attributes.getBucketName() + "." + attributes.getExtranetEndpoint() + "/" + path.getKey();
    }

    public String getIntranetUri(S3UriPath path) {
        return "https://" + attributes.getBucketName() + "." + attributes.getIntranetEndpoint() + "/" + path.getKey();
    }

    public String getOSSUri(S3UriPath path) {
        return uri.getScheme() + "://" + uri.getBucketName() + "/" + path.getKey();
    }

    public String getOSSExtranetUri(S3UriPath path) {
        return uri.getScheme() + "://" + attributes.getBucketName() + "." + attributes.getExtranetEndpoint() + "/" + path.getKey();
    }

    public String getOSSIntranetUri(S3UriPath path) {
        return uri.getScheme() + "://" + attributes.getBucketName() + "." + attributes.getIntranetEndpoint() + "/" + path.getKey();
    }

    protected String toHttp(String https) {
        return https.replaceFirst("^https://", "http://");
    }

    @Override
    public Collection<String> getUris(S3UriPath path) {
        Set<String> uris = new LinkedHashSet<>();
        for (Map.Entry<String, List<AliyunCDNConfiguration>> e : cdnIndex.entrySet()) {
            if (path.getKey().startsWith(e.getKey())) {
                for (AliyunCDNConfiguration cdn : e.getValue()) {
                    String uri = cdn.getCdn() + path.getKey().substring(e.getKey().length());
                    uris.add(uri);
                    uris.add(toHttp(uri));
                }
            }
        }
        String extranetUri = getExtranetUri(path);
        String intranetUri = getIntranetUri(path);
        uris.add(extranetUri);
        uris.add(toHttp(extranetUri));
        uris.add(intranetUri);
        uris.add(toHttp(intranetUri));
        uris.add(getOSSUri(path));
        uris.add(getOSSExtranetUri(path));
        uris.add(getOSSIntranetUri(path));
        return uris;
    }

    @Override
    public FileSystemAttributes readAttributes() {
        return attributes;
    }

    @Override
    public S3UriPath resolve(URI uri) {
        if (this.uri.getScheme().equals(uri.getScheme())) {
            S3UriPath path = new S3UriPath(uri);
            if (path.getAccessKeyId() == null &&
                    path.getSecretAccessKey() == null &&
                    path.getEndpoint() == null &&
                    path.getBucketName() != null &&
                    path.getBucketName().equals(this.uri.getBucketName()) &&
                    path.getKey() != null &&
                    path.getKey().startsWith(this.uri.getKey())) {
                return path;
            }
        }
        throw new IllegalArgumentException(uri.toString());
    }

    @Override
    public S3UriPath getParent(S3UriPath path) {
        return path.getParent();
    }

    @Override
    public boolean exists(S3UriPath path) {
        if (path.isRegularFile()) {
            log.trace("doesObjectExist: oss://{}/{}", path.getBucketName(), path.getKey());
            return oss.doesObjectExist(path.getBucketName(), path.getKey());
        } else {
            return true;
        }
    }

    @Override
    public AliyunOSSFile get(S3UriPath path) {
        return new AliyunOSSFile(this, path);
    }

    @Override
    public FileAttributes readAttributes(S3UriPath path) {
        if (path.isRegularFile()) {
            log.trace("getObjectMetadata: oss://{}/{}", path.getBucketName(), path.getKey());
            return new AliyunOSSFileAttributes(oss.getObjectMetadata(path.getBucketName(), path.getKey()));
        } else {
            return Files.emptyFileAttributes();
        }
    }

    @Override
    public FileReadableChannel newReadableChannel(S3UriPath path) {
        if (path.isRegularFile()) {
            log.trace("getObject: oss://{}/{}", path.getBucketName(), path.getKey());
            return new AliyunOSSFileReadableChannel(oss.getObject(path.getBucketName(), path.getKey()));
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    @Override
    public FileWritableChannel newWritableChannel(S3UriPath path) {
        if (path.isRegularFile()) {
            return new AliyunOSSFileWritableChannel(oss, path.getBucketName(), path.getKey());
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    @Override
    public DirectoryStream<AliyunOSSFile> newDirectoryStream(S3UriPath path) {
        if (path.isDirectory()) {
            FileStream<AliyunOSSFile> tree = new AliyunOSSFileTree(this, path, false);
            Iterable<AliyunOSSFile> list = new FilterIterable<>(tree,
                    new MappingPredicate<>(path::isSame, AliyunOSSFile::getPath).negate());
            return Files.newDirectoryStream(list, tree);
        }
        throw new UnsupportedOperationException(path + " is not directory");
    }

    @Override
    public void delete(S3UriPath path) {
        if (path.isDirectory()) {
            log.trace("deleteObjects: oss://{}/{}", path.getBucketName(), path.getKey());
            AliyunOSSObjectListingIterator iterator = new AliyunOSSObjectListingIterator(oss, path.getBucketName(), path.getKey());
            while (iterator.hasNext()) {
                ObjectListing objectListing = iterator.next();
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    oss.deleteObjects(new DeleteObjectsRequest(path.getBucketName()).withKeys(
                            objectListing.getObjectSummaries().stream()
                                    .map(OSSObjectSummary::getKey)
                                    .collect(Collectors.toList())
                    ));
                }
            }
        } else {
            log.trace("deleteObject: oss://{}/{}", path.getBucketName(), path.getKey());
            oss.deleteObject(path.getBucketName(), path.getKey());
        }
    }

    @Override
    public FileStream<AliyunOSSFile> tree(S3UriPath path) {
        if (path.isDirectory()) {
            return new AliyunOSSFileTree(this, path, true);
        } else {
            return Files.newFileStream(Collections.singleton(get(path)));
        }
    }

    @Override
    public FileStream<AliyunOSSFile> tree(S3UriPath path, int maxDepth) {
        if (path.isDirectory()) {
            switch (maxDepth) {
                case 0:
                    return Files.newFileStream(Collections.singleton(get(path)));
                case 1:
                    return new AliyunOSSFileTree(this, path, false);
                case Integer.MAX_VALUE:
                    return new AliyunOSSFileTree(this, path, true);
                default:
                    return new FileTree<>(this, path, maxDepth);
            }
        } else {
            return Files.newFileStream(Collections.singleton(get(path)));
        }
    }

    @Override
    public FileWatchService<S3UriPath, AliyunOSSFile> getWatchService() {
        if (watchService == null) {
            throw new UnsupportedOperationException("WatchService");
        }
        return watchService;
    }

    public void setWatchService(FileWatchService<S3UriPath, AliyunOSSFile> watchService) {
        if (this.watchService != null) {
            throw new IllegalStateException();
        }
        this.watchService = watchService;
    }

    @Override
    public void close() throws Exception {
        log.debug("close AliyunOSSFileSystem: {}", uri);
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
