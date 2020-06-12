package cc.whohow;

import cc.whohow.fs.ObjectFile;
import cc.whohow.fs.provider.aliyun.oss.AliyunOSSObjectManager;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.Credentials;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.DefaultCredentials;
import com.aliyun.oss.model.Bucket;
import org.junit.Test;

import java.util.List;

public class TestAliyunOSS {
    private static final String accessKeyId = "";
    private static final String secretAccessKey = "";

    @Test
    public void testListBucket() {
        Credentials credentials = new DefaultCredentials(accessKeyId, secretAccessKey);
        OSS oss = new OSSClient("https://oss.aliyuncs.com", new DefaultCredentialProvider(credentials), new ClientConfiguration());
        List<Bucket> bucketList = oss.listBuckets();
        for (Bucket bucket : bucketList) {
            System.out.println(bucket.getName());
            System.out.println(bucket.getExtranetEndpoint());
            System.out.println(bucket.getIntranetEndpoint());
            System.out.println();
        }
        oss.shutdown();
    }

    @Test
    public void testObjectFactory() throws Exception {
        try (AliyunOSSObjectManager objectFactory = new AliyunOSSObjectManager()) {
            ObjectFile objectFile = objectFactory.get(String.format("oss://%s:%s@yt-conf.oss-cn-hangzhou.aliyuncs.com/dev/api-yitong-com-base-v1/vfs.yml", accessKeyId, secretAccessKey));
            System.out.println(objectFile.readUtf8());
        }
    }
}
