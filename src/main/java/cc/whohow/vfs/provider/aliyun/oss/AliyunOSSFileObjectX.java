package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.provider.s3.S3FileName;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import org.apache.commons.vfs2.FileSystemException;

public class AliyunOSSFileObjectX extends AliyunOSSFileObject {
    protected final OSSClient oss;

    public AliyunOSSFileObjectX(String uri) {
        this(new S3FileName(uri));
    }

    public AliyunOSSFileObjectX(S3FileName name) {
        super(null, name);
        oss = new OSSClient(AliyunOSSEndpoints.getEndpoint(name.getEndpoint()),
                new DefaultCredentialProvider(name.getAccessKeyId(), name.getSecretAccessKey()),
                new ClientConfiguration());
    }

    @Override
    public OSS getOSS() {
        return oss;
    }

    @Override
    public void close() throws FileSystemException {
        try {
            oss.shutdown();
        } finally {
            super.close();
        }
    }
}
