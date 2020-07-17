package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.provider.GenericFileCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class QcloudCOSCopy extends GenericFileCopy<QcloudCOSFile, QcloudCOSFile> {
    private static final Logger log = LogManager.getLogger(QcloudCOSCopy.class);

    public QcloudCOSCopy(QcloudCOSFile source, QcloudCOSFile target) {
        super(source, target);
    }

    protected QcloudCOSFile copyFile(QcloudCOSFile source, QcloudCOSFile target) throws Exception {
        if (source.getFileSystem().getCOS().equals(target.getFileSystem().getCOS())) {
            log.trace("copyObject: cos://{}/{} -> cos://{}/{}",
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            source.getFileSystem().getCOS().copyObject(
                    source.getPath().getBucketName(), source.getPath().getKey(),
                    target.getPath().getBucketName(), target.getPath().getKey());
            return target;
        } else {
            return transferFile(source, target);
        }
    }

    @Override
    protected CompletableFuture<QcloudCOSFile> copyFileAsync(QcloudCOSFile source, QcloudCOSFile target, ExecutorService executor) {
        return new QcloudCOSCopy(source, target).copyFileAsync(executor);
    }

    @Override
    public String toString() {
        return "QcloudCOSCopy " + source + " " + target;
    }
}
