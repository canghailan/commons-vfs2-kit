package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.provider.StreamCopy;

import java.io.IOException;

public class AliyunOSSCopy extends StreamCopy.Parallel<AliyunOSSFile, AliyunOSSFile> {
    public AliyunOSSCopy(AliyunOSSFile source, AliyunOSSFile target) {
        super(source, target);
    }

    @Override
    protected AliyunOSSFile copyFile(AliyunOSSFile source, AliyunOSSFile target) throws IOException {
        return super.copyFile(source, target);
    }
}
