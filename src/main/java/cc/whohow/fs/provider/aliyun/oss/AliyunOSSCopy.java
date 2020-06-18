package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Copy;
import cc.whohow.fs.provider.AsyncCopy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AliyunOSSCopy extends AsyncCopy<AliyunOSSFile, AliyunOSSFile> {
    public AliyunOSSCopy(AliyunOSSFile source, AliyunOSSFile target, ExecutorService executor) {
        super(source, target, executor);
    }

    @Override
    protected CompletableFuture<AliyunOSSFile> copyFile(AliyunOSSFile source, AliyunOSSFile target) {
        // TODO
        return super.copyFile(source, target);
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
