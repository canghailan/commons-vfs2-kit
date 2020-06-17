package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Copy;
import cc.whohow.fs.provider.StreamCopy;

import java.io.IOException;

public class AliyunOSSCopy extends StreamCopy.Parallel<AliyunOSSFile, AliyunOSSFile> {
    public AliyunOSSCopy(AliyunOSSFile source, AliyunOSSFile target) {
        super(source, target);
    }

    @Override
    protected AliyunOSSFile copyFile(AliyunOSSFile source, AliyunOSSFile target) throws IOException {
        // TODO
        return super.copyFile(source, target);
    }

    @Override
    protected Copy<AliyunOSSFile, AliyunOSSFile> newFileCopy(AliyunOSSFile source, AliyunOSSFile target) {
        return new AliyunOSSCopy(source, target);
    }

    @Override
    public String toString() {
        return "aliyun-oss-copy " + source + " " + target;
    }
}
