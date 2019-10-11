package cc.whohow.vfs.provider.cos;

import com.qcloud.cos.COS;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.region.Region;
import org.apache.commons.vfs2.FileSystemException;

public class QcloudCOSFileObjectX extends QcloudCOSFileObject {
    protected final COSClient cos;

    public QcloudCOSFileObjectX(String uri) {
        this(new QcloudCOSFileName(uri));
    }

    public QcloudCOSFileObjectX(QcloudCOSFileName name) {
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
