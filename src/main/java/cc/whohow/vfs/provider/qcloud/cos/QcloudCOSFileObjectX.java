package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.vfs.provider.s3.S3FileName;
import com.qcloud.cos.COS;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.region.Region;
import org.apache.commons.vfs2.FileSystemException;

public class QcloudCOSFileObjectX extends QcloudCOSFileObject {
    protected final COSClient cos;

    public QcloudCOSFileObjectX(String uri) {
        this(new S3FileName(uri));
    }

    public QcloudCOSFileObjectX(S3FileName name) {
        super(null, name);
        cos = new COSClient(new BasicCOSCredentials(name.getAccessKeyId(), name.getSecretAccessKey()), new ClientConfig(new Region(name.getEndpoint())));
    }

    @Override
    public COS getCOS() {
        return cos;
    }

    @Override
    public void close() throws FileSystemException {
        try {
            cos.shutdown();
        } finally {
            super.close();
        }
    }
}
