package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Copy;
import cc.whohow.fs.provider.AsyncCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AliyunOSSCopy extends AsyncCopy<AliyunOSSFile, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSCopy.class);

    public AliyunOSSCopy(AliyunOSSFile source, AliyunOSSFile target, ExecutorService executor) {
        super(source, target, executor);
    }

    @Override
    protected CompletableFuture<AliyunOSSFile> copyFile(AliyunOSSFile source, AliyunOSSFile target) {
        if (source.getFileSystem().getOSS().equals(target.getFileSystem().getOSS())) {
            log.trace("copyObject: oss://{}/{} -> oss://{}/{}",
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            source.getFileSystem().getOSS().copyObject(
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            return CompletableFuture.completedFuture(target);
        } else {
            return super.copyFile(source, target);
        }
    }

    @Override
    protected Copy<AliyunOSSFile, AliyunOSSFile> newFileCopy(AliyunOSSFile source, AliyunOSSFile target) {
        return new AliyunOSSCopy(source, target, executor);
    }

    @Override
    public String toString() {
        return "aliyun-oss-copy " + source + " " + target;
    }
}