package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.*;
import cc.whohow.vfs.log.LogProxy;
import cc.whohow.vfs.provider.s3.S3FileName;
import cc.whohow.vfs.provider.s3.S3Uri;
import cc.whohow.vfs.serialize.YamlSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.qcloud.cos.COS;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.StringUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QcloudCOSFileSystemProvider extends AbstractVfsComponent implements FileSystemProviderX {
    private static final Set<Capability> CAPABILITIES = Collections.unmodifiableSet(EnumSet.of(
            Capability.READ_CONTENT,
            Capability.WRITE_CONTENT,
            Capability.ATTRIBUTES,
            Capability.LAST_MODIFIED,
            Capability.GET_LAST_MODIFIED,
            Capability.CREATE,
            Capability.DELETE,
            Capability.RENAME,
            Capability.GET_TYPE,
            Capability.LIST_CHILDREN,
            Capability.URI));
    protected Set<COSCredentials> credentials = new LinkedHashSet<>();
    protected Map<String, S3Uri> buckets = new TreeMap<>();
    protected Map<S3Uri, COSClient> clients = new ConcurrentHashMap<>();
    protected Map<String, QcloudCOSFileSystem> fileSystems = new ConcurrentHashMap<>();

    @Override
    public String getScheme() {
        return "cos";
    }

    protected COSClient newCOS(S3Uri uri) {
        return new COSClient(new BasicCOSCredentials(uri.getAccessKeyId(), uri.getSecretAccessKey()), new ClientConfig(new Region(uri.getEndpoint())));
    }

    protected COS getCOS(S3Uri uri) {
        if (uri == null ||
                !Objects.equals(uri.getScheme(), getScheme()) ||
                StringUtils.isNullOrEmpty(uri.getAccessKeyId()) ||
                StringUtils.isNullOrEmpty(uri.getSecretAccessKey()) ||
                StringUtils.isNullOrEmpty(uri.getEndpoint())) {
            throw new IllegalArgumentException(Objects.toString(uri));
        }
        COS cos = clients.computeIfAbsent(new S3Uri(uri.getScheme(), uri.getAccessKeyId(), uri.getSecretAccessKey(), null, uri.getEndpoint(), null), this::newCOS);
        return LogProxy.newProxyInstance(getLogger(), cos, COS.class);
    }

    protected QcloudCOSFileSystem newFileSystem(S3Uri uri) {
        if (uri == null || StringUtils.isNullOrEmpty(uri.getBucketName())) {
            throw new IllegalArgumentException(Objects.toString(uri));
        }
        if (!Objects.equals(uri.getScheme(), getScheme()) ||
                StringUtils.isNullOrEmpty(uri.getAccessKeyId()) ||
                StringUtils.isNullOrEmpty(uri.getSecretAccessKey()) ||
                StringUtils.isNullOrEmpty(uri.getEndpoint())) {
            return new QcloudCOSFileSystem(this, uri.getBucketName(), getCOS(buckets.get(uri.getBucketName())));
        } else {
            return new QcloudCOSFileSystem(this, uri.getBucketName(), getCOS(uri));
        }
    }

    protected QcloudCOSFileSystem getFileSystem(S3Uri uri) throws FileSystemException {
        QcloudCOSFileSystem fileSystem = fileSystems.get(uri.getBucketName());
        if (fileSystem == null) {
            synchronized (this) {
                fileSystem = fileSystems.get(uri.getBucketName());
                if (fileSystem == null) {
                    fileSystem = newFileSystem(uri);
                    fileSystem.setContext(getContext());
                    fileSystem.init();
                }
                fileSystems.put(uri.getBucketName(), fileSystem);
            }
        }
        return fileSystem;
    }

    @Override
    public FileSystemX getFileSystem(String uri) throws FileSystemException {
        return getFileSystem(new S3Uri(uri));
    }

    @Override
    public FileSystemX findFileSystem(String uri) throws FileSystemException {
        return getFileSystem(new S3Uri(uri));
    }

    @Override
    public FileName getFileName(String uri) throws FileSystemException {
        return new S3FileName(uri);
    }

    @Override
    public FileObjectX getFileObject(String uri) throws FileSystemException {
        S3FileName fileName = new S3FileName(uri);
        return new QcloudCOSFileObject(getFileSystem(fileName), fileName);
    }

    @Override
    public FileOperationsX getFileOperations() throws FileSystemException {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }

    @Override
    public void init() throws FileSystemException {
        VirtualFileSystem vfs = (VirtualFileSystem) getContext().getFileSystemManager();

        // credentials
        try (DirectoryStream<FileObjectX> list = vfs.resolveFile("conf:/providers/qcloud-cos/credentials/").list()) {
            for (FileObjectX credential : list) {
                JsonNode value = YamlSerializer.get().deserialize(credential);
                credentials.add(new BasicCOSCredentials(value.get("accessKeyId").textValue(), value.get("secretAccessKey").textValue()));
            }
        } catch (FileSystemException e) {
            throw e;
        } catch (IOException e) {
            throw new FileSystemException(e);
        }

        // buckets
        for (COSCredentials credential : credentials) {
            COSClient cos = new COSClient(credential, new ClientConfig());
            try {
                List<Bucket> buckets = cos.listBuckets();
                for (Bucket bucket : buckets) {
                    this.buckets.put(bucket.getName(),
                            new S3Uri(getScheme(), credential.getCOSAccessKeyId(), credential.getCOSSecretKey(), bucket.getName(), bucket.getLocation(), null));
                }
            } finally {
                cos.shutdown();
            }
        }
    }

    @Override
    public void close() {
        for (COSClient cos : clients.values()) {
            try {
                cos.shutdown();
            } catch (Exception e) {
                getLogger().debug("shutdown cos " + cos.getClientConfig().getRegion(), e);
            }
        }
    }

    @Override
    public String toString() {
        return buckets.values().stream()
                .map(S3Uri::toPublic)
                .map(S3Uri::toString)
                .collect(Collectors.joining("\n"));
    }
}
