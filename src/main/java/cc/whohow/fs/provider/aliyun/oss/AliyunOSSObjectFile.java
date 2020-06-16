package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.ObjectFile;
import cc.whohow.fs.io.FileReadableStream;
import cc.whohow.fs.provider.s3.S3Uri;
import com.aliyun.oss.OSS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class AliyunOSSObjectFile implements ObjectFile {
    private static final Logger log = LogManager.getLogger(AliyunOSSObjectFile.class);
    private final OSS oss;
    private final S3Uri uri;

    public AliyunOSSObjectFile(OSS oss, S3Uri uri) {
        this.oss = oss;
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri.toUri();
    }

    @Override
    public boolean exists() {
        log.trace("doesObjectExist: oss://{}/{}", uri.getBucketName(), uri.getKey());
        return oss.doesObjectExist(uri.getBucketName(), uri.getKey());
    }

    @Override
    public FileAttributes readAttributes() {
        log.trace("getObjectMetadata: oss://{}/{}", uri.getBucketName(), uri.getKey());
        return new AliyunOSSFileAttributes(oss.getObjectMetadata(uri.getBucketName(), uri.getKey()));
    }

    @Override
    public FileReadableChannel newReadableChannel() {
        log.trace("getObject: oss://{}/{}", uri.getBucketName(), uri.getKey());
        return new FileReadableStream(oss.getObject(uri.getBucketName(), uri.getKey()).getObjectContent());
    }

    @Override
    public FileWritableChannel newWritableChannel() {
        return new AliyunOSSFileWritableChannel(oss, uri.getBucketName(), uri.getKey());
    }

    @Override
    public void delete() {
        log.trace("deleteObject: oss://{}/{}", uri.getBucketName(), uri.getKey());
        oss.deleteObject(uri.getBucketName(), uri.getKey());
    }
}
