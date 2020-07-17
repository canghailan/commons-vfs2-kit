package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.provider.GenericFileCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AliyunOSSCopy extends GenericFileCopy<AliyunOSSFile, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSCopy.class);

    public AliyunOSSCopy(AliyunOSSFile source, AliyunOSSFile target) {
        super(source, target);
    }

    protected AliyunOSSFile copyFile(AliyunOSSFile source, AliyunOSSFile target) throws Exception {
        if (source.getFileSystem().getOSS().equals(target.getFileSystem().getOSS())) {
            log.trace("copyObject: oss://{}/{} -> oss://{}/{}",
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            source.getFileSystem().getOSS().copyObject(
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            return target;
        } else {
            return transferFile(source, target);
        }
    }

    @Override
    protected CompletableFuture<AliyunOSSFile> copyFileAsync(AliyunOSSFile source, AliyunOSSFile target, ExecutorService executor) {
        return new AliyunOSSCopy(source, target).copyFileAsync(executor);
    }

    @Override
    public String toString() {
        return "AliyunOSSCopy " + source + " " + target;
    }
}
