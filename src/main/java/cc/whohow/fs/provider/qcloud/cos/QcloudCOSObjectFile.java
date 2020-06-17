package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.ObjectFile;
import cc.whohow.fs.provider.s3.S3Uri;
import cc.whohow.fs.util.FileReadableStream;
import com.qcloud.cos.COS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class QcloudCOSObjectFile implements ObjectFile {
    private static final Logger log = LogManager.getLogger(QcloudCOSObjectFile.class);
    private final COS cos;
    private final S3Uri uri;

    public QcloudCOSObjectFile(COS cos, S3Uri uri) {
        this.cos = cos;
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri.toUri();
    }

    @Override
    public boolean exists() {
        log.trace("doesObjectExist: cos://{}/{}", uri.getBucketName(), uri.getKey());
        return cos.doesObjectExist(uri.getBucketName(), uri.getKey());
    }

    @Override
    public FileAttributes readAttributes() {
        log.trace("getObjectMetadata: cos://{}/{}", uri.getBucketName(), uri.getKey());
        return new QcloudCOSFileAttributes(cos.getObjectMetadata(uri.getBucketName(), uri.getKey()));
    }

    @Override
    public FileReadableChannel newReadableChannel() {
        log.trace("getObject: cos://{}/{}", uri.getBucketName(), uri.getKey());
        return new FileReadableStream(cos.getObject(uri.getBucketName(), uri.getKey()).getObjectContent());
    }

    @Override
    public FileWritableChannel newWritableChannel() {
        return new QcloudCOSFileWritableChannel(cos, uri.getBucketName(), uri.getKey());
    }

    @Override
    public void delete() {
        log.trace("deleteObject: cos://{}/{}", uri.getBucketName(), uri.getKey());
        cos.deleteObject(uri.getBucketName(), uri.getKey());
    }
}
