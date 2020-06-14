package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.*;
import cc.whohow.fs.channel.FileReadableStream;
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
import java.util.Collections;
import java.util.stream.Collectors;

public class AliyunOSSFileSystem implements FileSystem<S3UriPath, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSFileSystem.class);
    protected final S3Uri uri;
    protected final AliyunOSSFileSystemAttributes attributes;
    protected final OSS oss;

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

    public OSS getOSS() {
        return oss;
    }

    @Override
    public URI getUri() {
        return uri.toUri();
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
                    path.getKey() != null &&
                    path.getBucketName().equals(this.uri.getBucketName()) &&
                    path.getKey().startsWith(this.uri.getKey())) {
                return path;
            }
        }
        throw new IllegalArgumentException(uri.toString());
    }

    @Override
    public S3UriPath getParent(S3UriPath path) {
        return null;
    }

    @Override
    public boolean exists(S3UriPath path) {
        log.trace("doesObjectExist: oss://{}/{}", path.getBucketName(), path.getKey());
        return oss.doesObjectExist(path.getBucketName(), path.getKey());
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
            return new FileReadableStream(oss.getObject(path.getBucketName(), path.getKey()).getObjectContent());
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
    public void close() throws Exception {
        log.debug("close AliyunOSSFileSystem: {}", uri);
    }
}
