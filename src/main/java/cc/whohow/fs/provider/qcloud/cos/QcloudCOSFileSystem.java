package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.qcloud.cdn.QcloudCDNConfiguration;
import cc.whohow.fs.provider.s3.S3Uri;
import cc.whohow.fs.provider.s3.S3UriPath;
import cc.whohow.fs.util.FileTree;
import cc.whohow.fs.util.Files;
import cc.whohow.fs.util.FilterIterable;
import cc.whohow.fs.util.MappingPredicate;
import com.qcloud.cos.COS;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.stream.Collectors;

public class QcloudCOSFileSystem implements FileSystem<S3UriPath, QcloudCOSFile> {
    private static final Logger log = LogManager.getLogger(QcloudCOSFileSystem.class);
    protected final S3Uri uri;
    protected final QcloudCOSFileSystemAttributes attributes;
    protected final COS cos;
    // key -> [cdn]
    protected final Map<String, List<QcloudCDNConfiguration>> cdnIndex = new LinkedHashMap<>();
    protected volatile FileWatchService<S3UriPath, QcloudCOSFile> watchService;

    public QcloudCOSFileSystem(S3Uri uri, QcloudCOSFileSystemAttributes attributes, COS cos) {
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
        this.cos = cos;
    }

    public void addCdn(QcloudCDNConfiguration configuration) {
        S3Uri origin = new S3Uri(URI.create(configuration.getOrigin()));
        if (!origin.getBucketName().equals(uri.getBucketName())) {
            throw new IllegalArgumentException(configuration.getOrigin());
        }
        cdnIndex.computeIfAbsent(origin.getKey(), key -> new ArrayList<>())
                .add(configuration);
    }

    public COS getCOS() {
        return cos;
    }

    @Override
    public URI getUri() {
        return uri.toUri();
    }

    @Override
    public String getPublicUri(S3UriPath path) {
        for (Map.Entry<String, List<QcloudCDNConfiguration>> e : cdnIndex.entrySet()) {
            if (path.getKey().startsWith(e.getKey())) {
                QcloudCDNConfiguration cdn = e.getValue().get(0);
                return cdn.getCdn() + path.getKey().substring(e.getKey().length());
            }
        }
        return "https://" + attributes.getBucketName() + ".cos." + attributes.getLocation() + ".myqcloud.com/" + path.getKey();
    }

    public String getCOSUri(S3UriPath path) {
        return uri.getScheme() + "://" + uri.getBucketName() + "/" + path.getKey();
    }

    public String getCOSLocationUri(S3UriPath path) {
        return uri.getScheme() + "://" + attributes.getBucketName() + "." + attributes.getLocation() + "/" + path.getKey();
    }

    protected String toHttp(String https) {
        return https.replaceFirst("^https://", "http://");
    }

    @Override
    public Collection<String> getUris(S3UriPath path) {
        Set<String> uris = new LinkedHashSet<>();
        for (Map.Entry<String, List<QcloudCDNConfiguration>> e : cdnIndex.entrySet()) {
            if (path.getKey().startsWith(e.getKey())) {
                for (QcloudCDNConfiguration cdn : e.getValue()) {
                    String uri = cdn.getCdn() + path.getKey().substring(e.getKey().length());
                    uris.add(uri);
                    uris.add(toHttp(uri));
                }
            }
        }
        String http = getPublicUri(path);
        uris.add(http);
        uris.add(toHttp(http));
        uris.add(getCOSUri(path));
        uris.add(getCOSLocationUri(path));
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
            log.trace("doesObjectExist: cos://{}/{}", path.getBucketName(), path.getKey());
            return cos.doesObjectExist(path.getBucketName(), path.getKey());
        } else {
            return true;
        }
    }

    @Override
    public QcloudCOSFile get(S3UriPath path) {
        return new QcloudCOSFile(this, path);
    }

    @Override
    public FileAttributes readAttributes(S3UriPath path) {
        if (path.isRegularFile()) {
            log.trace("getObjectMetadata: cos://{}/{}", path.getBucketName(), path.getKey());
            return new QcloudCOSFileAttributes(cos.getObjectMetadata(path.getBucketName(), path.getKey()));
        } else {
            return Files.emptyFileAttributes();
        }
    }

    @Override
    public FileReadableChannel newReadableChannel(S3UriPath path) {
        if (path.isRegularFile()) {
            log.trace("getObject: cos://{}/{}", path.getBucketName(), path.getKey());
            return new QcloudCOSFileReadableChannel(cos.getObject(path.getBucketName(), path.getKey()));
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    @Override
    public FileWritableChannel newWritableChannel(S3UriPath path) {
        if (path.isRegularFile()) {
            return new QcloudCOSFileWritableChannel(cos, path.getBucketName(), path.getKey());
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    @Override
    public DirectoryStream<QcloudCOSFile> newDirectoryStream(S3UriPath path) {
        if (path.isDirectory()) {
            FileStream<QcloudCOSFile> tree = new QcloudCOSFileTree(this, path, false);
            Iterable<QcloudCOSFile> list = new FilterIterable<>(tree,
                    new MappingPredicate<>(path::isSame, QcloudCOSFile::getPath).negate());
            return Files.newDirectoryStream(list, tree);
        }
        throw new UnsupportedOperationException(path + " is not directory");
    }

    @Override
    public void delete(S3UriPath path) {
        if (path.isDirectory()) {
            log.trace("deleteObjects: cos://{}/{}", path.getBucketName(), path.getKey());
            QcloudCOSObjectListingIterator iterator = new QcloudCOSObjectListingIterator(cos, path.getBucketName(), path.getKey());
            while (iterator.hasNext()) {
                ObjectListing objectListing = iterator.next();
                if (!objectListing.getObjectSummaries().isEmpty()) {
                    List<DeleteObjectsRequest.KeyVersion> keys = objectListing.getObjectSummaries().stream()
                            .map(COSObjectSummary::getKey)
                            .map(DeleteObjectsRequest.KeyVersion::new)
                            .collect(Collectors.toList());
                    getCOS().deleteObjects(new DeleteObjectsRequest(path.getBucketName()).withKeys(keys));
                }
            }
        } else {
            log.trace("deleteObject: cos://{}/{}", path.getBucketName(), path.getKey());
            cos.deleteObject(path.getBucketName(), path.getKey());
        }
    }

    @Override
    public FileStream<QcloudCOSFile> tree(S3UriPath path) {
        if (path.isDirectory()) {
            return new QcloudCOSFileTree(this, path, true);
        } else {
            return Files.newFileStream(Collections.singleton(get(path)));
        }
    }

    @Override
    public FileStream<QcloudCOSFile> tree(S3UriPath path, int maxDepth) {
        if (path.isDirectory()) {
            switch (maxDepth) {
                case 0:
                    return Files.newFileStream(Collections.singleton(get(path)));
                case 1:
                    return new QcloudCOSFileTree(this, path, false);
                case Integer.MAX_VALUE:
                    return new QcloudCOSFileTree(this, path, true);
                default:
                    return new FileTree<>(this, path, maxDepth);
            }
        } else {
            return Files.newFileStream(Collections.singleton(get(path)));
        }
    }

    @Override
    public FileWatchService<S3UriPath, QcloudCOSFile> getWatchService() {
        if (watchService == null) {
            throw new UnsupportedOperationException("WatchService");
        }
        return watchService;
    }

    public void setWatchService(FileWatchService<S3UriPath, QcloudCOSFile> watchService) {
        if (this.watchService != null) {
            throw new IllegalStateException();
        }
        this.watchService = watchService;
    }

    @Override
    public void close() throws Exception {
        log.debug("close QcloudCOSFileSystem: {}", this);
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}