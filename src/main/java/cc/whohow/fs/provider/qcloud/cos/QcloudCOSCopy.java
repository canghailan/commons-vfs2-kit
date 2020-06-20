package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.Copy;
import cc.whohow.fs.provider.AsyncCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class QcloudCOSCopy extends AsyncCopy<QcloudCOSFile, QcloudCOSFile> {
    private static final Logger log = LogManager.getLogger(QcloudCOSCopy.class);

    public QcloudCOSCopy(QcloudCOSFile source, QcloudCOSFile target, ExecutorService executor) {
        super(source, target, executor);
    }

    @Override
    protected CompletableFuture<QcloudCOSFile> copyFile(QcloudCOSFile source, QcloudCOSFile target) {
        if (source.getFileSystem().getCOS().equals(target.getFileSystem().getCOS())) {
            log.trace("copyObject: cos://{}/{} -> cos://{}/{}",
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            source.getFileSystem().getCOS().copyObject(
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            return CompletableFuture.completedFuture(target);
        } else {
            return super.copyFile(source, target);
        }
    }

    @Override
    protected Copy<QcloudCOSFile, QcloudCOSFile> newFileCopy(QcloudCOSFile source, QcloudCOSFile target) {
        return new QcloudCOSCopy(source, target, executor);
    }

    @Override
    public String toString() {
        return "qcloud-cos-copy " + source + " " + target;
    }
}
