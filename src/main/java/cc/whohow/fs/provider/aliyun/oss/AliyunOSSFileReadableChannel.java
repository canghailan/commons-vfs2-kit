package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.util.FileReadableStream;
import com.aliyun.oss.model.OSSObject;

import java.util.Optional;

public class AliyunOSSFileReadableChannel extends FileReadableStream {
    protected final OSSObject object;

    public AliyunOSSFileReadableChannel(OSSObject object) {
        super(object.getObjectContent());
        this.object = object;
    }

    public AliyunOSSFileReadableChannel(OSSObject object, Runnable onClose) {
        super(object.getObjectContent(), onClose);
        this.object = object;
    }

    @Override
    public long size() {
        return object.getObjectMetadata().getContentLength();
    }

    @Override
    public Optional<FileAttributes> readFileAttributes() {
        return Optional.of(new AliyunOSSFileAttributes(object.getObjectMetadata()));
    }
}
