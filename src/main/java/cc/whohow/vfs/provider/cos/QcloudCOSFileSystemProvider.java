package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.*;
import cc.whohow.vfs.type.DataType;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.JsonType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QcloudCOSFileSystemProvider extends AbstractVfsComponent implements CloudFileSystemProvider {
    protected Set<COSCredentials> credentials = new LinkedHashSet<>();
    protected Map<String, QcloudCOSUri> buckets = new TreeMap<>();
    protected Map<QcloudCOSUri, COSClient> clients = new ConcurrentHashMap<>();
    protected Map<String, QcloudCOSFileSystem> fileSystems = new ConcurrentHashMap<>();

    @Override
    public String getScheme() {
        return "cos";
    }

    protected COSClient newCOS(QcloudCOSUri uri) {
        return new COSClient(new BasicCOSCredentials(uri.getAccessKeyId(), uri.getSecretAccessKey()), new ClientConfig(new Region(uri.getEndpoint())));
    }

    protected COSClient getCOS(QcloudCOSUri uri) {
        if (uri == null ||
                StringUtils.isNullOrEmpty(uri.getAccessKeyId()) ||
                StringUtils.isNullOrEmpty(uri.getSecretAccessKey()) ||
                StringUtils.isNullOrEmpty(uri.getEndpoint())) {
            throw new IllegalArgumentException(Objects.toString(uri));
        }
        return clients.computeIfAbsent(new QcloudCOSUri(uri.getAccessKeyId(), uri.getSecretAccessKey(), null, uri.getEndpoint(), null), this::newCOS);
    }

    protected QcloudCOSFileSystem newFileSystem(QcloudCOSUri uri) {
        if (uri == null || StringUtils.isNullOrEmpty(uri.getBucketName())) {
            throw new IllegalArgumentException(Objects.toString(uri));
        }
        if (StringUtils.isNullOrEmpty(uri.getAccessKeyId()) ||
                StringUtils.isNullOrEmpty(uri.getSecretAccessKey()) ||
                StringUtils.isNullOrEmpty(uri.getEndpoint())) {
            return new QcloudCOSFileSystem(this, uri.getBucketName(), getCOS(buckets.get(uri.getBucketName())));
        } else {
            return new QcloudCOSFileSystem(this, uri.getBucketName(), getCOS(uri));
        }
    }

    protected QcloudCOSFileSystem getFileSystem(QcloudCOSUri uri) {
        QcloudCOSFileSystem fileSystem = fileSystems.get(uri.getBucketName());
        if (fileSystem == null) {
            synchronized (this) {
                fileSystem = fileSystems.get(uri.getBucketName());
                if (fileSystem == null) {
                    fileSystem = newFileSystem(uri);
                }
                fileSystems.put(uri.getBucketName(), fileSystem);
            }
        }
        return fileSystem;
    }

    @Override
    public CloudFileSystem getFileSystem(String uri) throws FileSystemException {
        return getFileSystem(new QcloudCOSUri(uri));
    }

    @Override
    public CloudFileSystem findFileSystem(String uri) throws FileSystemException {
        return getFileSystem(new QcloudCOSUri(uri));
    }

    @Override
    public FileName getFileName(String uri) throws FileSystemException {
        return new QcloudCOSFileName(uri);
    }

    @Override
    public CloudFileObject getFileObject(String uri) throws FileSystemException {
        QcloudCOSFileName fileName = new QcloudCOSFileName(uri);
        return new QcloudCOSFileObject(getFileSystem(fileName), fileName);
    }

    @Override
    public CloudFileOperations getFileOperations() throws FileSystemException {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }

    @Override
    public void init() throws FileSystemException {
        VirtualFileSystem vfs = (VirtualFileSystem) getContext().getFileSystemManager();
        DataType<JsonNode> yaml = new JsonType<>(new YAMLMapper(), JsonNode.class);

        // credentials
        try (CloudFileObjectList list = vfs.resolveFile("conf:/providers/cos/credentials").list()) {
            for (CloudFileObject credential : list) {
                JsonNode value = new FileValue<>(credential, yaml).get();
                credentials.add(new BasicCOSCredentials(value.get("secretId").textValue(), value.get("secretKey").textValue()));
            }
        }

        // buckets
        for (COSCredentials credential : credentials) {
            COSClient cos = new COSClient(credential, new ClientConfig());
            try {
                List<Bucket> buckets = cos.listBuckets();
                for (Bucket bucket : buckets) {
                    this.buckets.put(bucket.getName(),
                            new QcloudCOSUri(credential.getCOSAccessKeyId(), credential.getCOSSecretKey(), bucket.getName(), bucket.getLocation(), null));
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
                .map(QcloudCOSUri::toString)
                .collect(Collectors.joining("\n"));
    }
}
