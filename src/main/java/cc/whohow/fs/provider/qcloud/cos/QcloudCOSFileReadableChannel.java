package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.util.FileReadableStream;
import com.qcloud.cos.model.COSObject;

import java.util.Optional;

public class QcloudCOSFileReadableChannel extends FileReadableStream {
    protected final COSObject object;

    public QcloudCOSFileReadableChannel(COSObject object) {
        super(object.getObjectContent());
        this.object = object;
    }

    public QcloudCOSFileReadableChannel(COSObject object, Runnable onClose) {
        super(object.getObjectContent(), onClose);
        this.object = object;
    }

    @Override
    public long size() {
        return object.getObjectMetadata().getContentLength();
    }

    @Override
    public Optional<FileAttributes> readFileAttributes() {
        return Optional.of(new QcloudCOSFileAttributes(object.getObjectMetadata()));
    }
}
