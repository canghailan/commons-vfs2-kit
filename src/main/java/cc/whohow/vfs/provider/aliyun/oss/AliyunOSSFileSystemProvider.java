package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.*;
import cc.whohow.vfs.log.LogProxy;
import cc.whohow.vfs.provider.s3.S3FileName;
import cc.whohow.vfs.provider.s3.S3Uri;
import cc.whohow.vfs.type.DataType;
import cc.whohow.vfs.type.JsonType;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyun.oss.model.Bucket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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

public class AliyunOSSFileSystemProvider extends AbstractVfsComponent implements CloudFileSystemProvider {
    private static final Set<Capability> CAPABILITIES = Collections.unmodifiableSet(EnumSet.of(
            Capability.READ_CONTENT,
            Capability.WRITE_CONTENT,
            Capability.APPEND_CONTENT,
            Capability.ATTRIBUTES,
            Capability.LAST_MODIFIED,
            Capability.GET_LAST_MODIFIED,
            Capability.CREATE,
            Capability.DELETE,
            Capability.RENAME,
            Capability.GET_TYPE,
            Capability.LIST_CHILDREN,
            Capability.URI));
    protected Set<DefaultCredentials> credentials = new LinkedHashSet<>();
    protected ClientConfiguration clientConfiguration;
    protected Map<String, S3Uri> buckets = new TreeMap<>();
    protected Map<S3Uri, OSSClient> clients = new ConcurrentHashMap<>();
    protected Map<String, AliyunOSSFileSystem> fileSystems = new ConcurrentHashMap<>();

    @Override
    public String getScheme() {
        return "oss";
    }

    protected OSSClient newOSS(S3Uri uri) {
        return new OSSClient(
                AliyunOSSEndpoints.getEndpoint(uri.getEndpoint()),
                new DefaultCredentialProvider(uri.getAccessKeyId(), uri.getSecretAccessKey()),
                clientConfiguration);
    }

    protected OSS getOSS(S3Uri uri) {
        if (uri == null ||
                !Objects.equals(uri.getScheme(), getScheme()) ||
                StringUtils.isNullOrEmpty(uri.getAccessKeyId()) ||
                StringUtils.isNullOrEmpty(uri.getSecretAccessKey()) ||
                StringUtils.isNullOrEmpty(uri.getEndpoint())) {
            throw new IllegalArgumentException(Objects.toString(uri));
        }
        OSSClient oss = clients.computeIfAbsent(new S3Uri(uri.getScheme(), uri.getAccessKeyId(), uri.getSecretAccessKey(), null, uri.getEndpoint(), null), this::newOSS);
        return LogProxy.newProxyInstance(getLogger(), oss, OSS.class);
    }

    protected AliyunOSSFileSystem newFileSystem(S3Uri uri) {
        if (uri == null || StringUtils.isNullOrEmpty(uri.getBucketName())) {
            throw new IllegalArgumentException(Objects.toString(uri));
        }
        if (!Objects.equals(uri.getScheme(), getScheme()) ||
                StringUtils.isNullOrEmpty(uri.getAccessKeyId()) ||
                StringUtils.isNullOrEmpty(uri.getSecretAccessKey()) ||
                StringUtils.isNullOrEmpty(uri.getEndpoint())) {
            return new AliyunOSSFileSystem(this, uri.getBucketName(), getOSS(buckets.get(uri.getBucketName())));
        } else {
            return new AliyunOSSFileSystem(this, uri.getBucketName(), getOSS(uri));
        }
    }

    protected AliyunOSSFileSystem getFileSystem(S3Uri uri) throws FileSystemException {
        AliyunOSSFileSystem fileSystem = fileSystems.get(uri.getBucketName());
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
    public CloudFileSystem getFileSystem(String uri) throws FileSystemException {
        return getFileSystem(new S3Uri(uri));
    }

    @Override
    public CloudFileSystem findFileSystem(String uri) throws FileSystemException {
        return getFileSystem(new S3Uri(uri));
    }

    @Override
    public FileName getFileName(String uri) throws FileSystemException {
        return new S3FileName(uri);
    }

    @Override
    public CloudFileObject getFileObject(String uri) throws FileSystemException {
        S3FileName fileName = new S3FileName(uri);
        return new AliyunOSSFileObject(getFileSystem(fileName), fileName);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public CloudFileOperations getFileOperations() throws FileSystemException {
        return null;
    }

    @Override
    public void init() throws FileSystemException {
        VirtualFileSystem vfs = (VirtualFileSystem) getContext().getFileSystemManager();
        DataType<JsonNode> yaml = new JsonType<>(new YAMLMapper(), JsonNode.class);

        // credentials
        try (DirectoryStream<CloudFileObject> list = vfs.resolveFile("conf:/providers/aliyun-oss/credentials/").list()) {
            for (CloudFileObject credential : list) {
                JsonNode value = new FileValue<>(credential, yaml).get();
                credentials.add(new DefaultCredentials(value.get("accessKeyId").textValue(), value.get("secretAccessKey").textValue()));
            }
        } catch (FileSystemException e) {
            throw e;
        } catch (IOException e) {
            throw new FileSystemException(e);
        }

        // clientConfiguration
        clientConfiguration = new ClientConfiguration();

        // buckets
        for (DefaultCredentials credential : credentials) {
            OSSClient cos = new OSSClient(AliyunOSSEndpoints.getDefaultEndpoint(), new DefaultCredentialProvider(credential), clientConfiguration);
            try {
                List<Bucket> buckets = cos.listBuckets();
                for (Bucket bucket : buckets) {
                    this.buckets.put(bucket.getName(),
                            new S3Uri(getScheme(), credential.getAccessKeyId(), credential.getSecretAccessKey(), bucket.getName(), bucket.getExtranetEndpoint(), null));
                }
            } finally {
                cos.shutdown();
            }
        }
    }

    @Override
    public void close() {
        for (OSSClient oss : clients.values()) {
            try {
                oss.shutdown();
            } catch (Exception e) {
                getLogger().debug("shutdown oss " + oss.getEndpoint(), e);
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

    public String getURL(S3FileName name, Date expire) {
        return null;
    }
}
