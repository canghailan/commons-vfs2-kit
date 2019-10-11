package cc.whohow.vfs.provider.cos;

import cc.whohow.vfs.io.ByteBufferReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import com.qcloud.cos.COS;
import com.qcloud.cos.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * COS写入流，通过MultipartUpload实现
 */
public class QcloudCOSWritableChannel extends WritableChannel {
    private static final int MIN_PART_SIZE = 1024 * 1024;

    private final COS cos;
    private final String bucketName;
    private final String key;
    private String uploadId;
    private List<PartETag> partETags = new ArrayList<>();
    private byte[] buffer;
    private int length;

    public QcloudCOSWritableChannel(COS cos, String bucketName, String key) {
        this(cos, bucketName, key, 2 * MIN_PART_SIZE);
    }

    public QcloudCOSWritableChannel(COS cos, String bucketName, String key, int bufferSize) {
        if (bufferSize < MIN_PART_SIZE) {
            throw new IllegalArgumentException("Illegal buffer size: " + bufferSize);
        }
        this.cos = cos;
        this.bucketName = bucketName;
        this.key = key;
        this.buffer = new byte[bufferSize];
        this.length = 0;
    }

    public COS getCOS() {
        return cos;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b});
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (uploadId == null) {
            uploadId = cos.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key))
                    .getUploadId();
        }

        if (len < buffer.length) {
            int r = buffer.length - length;
            if (r > len) {
                writeBuffer(b, off,  len);
            } else if (r == len) {
                writeBuffer(b, off,  len);
                flushBuffer(false);
            } else {
                writeBuffer(b, off, r);
                flushBuffer(false);
                writeBuffer(b, off + r, len - r);
            }
        } else {
            if (length == 0) {
                upload(b, off, len, false);
            } else if (length >= MIN_PART_SIZE) {
                flushBuffer(false);
                upload(b, off, len, false);
            } else {
                int r = buffer.length - length;
                writeBuffer(b, off, r);
                flushBuffer(false);
                if (len - r >= buffer.length) {
                    upload(b, off + r, len - r, false);
                } else {
                    writeBuffer(b, off + r, len - r);
                }
            }
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (length > 0) {
            flushBuffer(true);
        }
        cos.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadId, partETags));
    }

    private synchronized void writeBuffer(byte[] b, int off, int len) {
        System.arraycopy(b, off,buffer, length,  len);
        length += len;
    }

    private synchronized void flushBuffer(boolean close) {
        upload(buffer, 0, length, close);
        length = 0;
    }

    private synchronized void upload(byte[] buffer, int offset, int length, boolean close) {
        int partNumber = partETags.size() + 1;
        UploadPartResult result = cos.uploadPart(new UploadPartRequest()
                .withBucketName(bucketName)
                .withKey(key)
                .withUploadId(uploadId)
                .withPartNumber(partNumber)
                .withInputStream(new ByteArrayInputStream(buffer, 0, length))
                .withPartSize(length)
                .withLastPart(close)
        );
        partETags.add(new PartETag(partNumber, result.getETag()));
    }

    @Override
    public int writeAll(ByteBuffer buffer) throws IOException {
        return (int) transferFrom(new ByteBufferReadableChannel(buffer));
    }

    @Override
    public long transferFrom(InputStream stream) throws IOException {
        return cos.putObject(bucketName, key, stream, new ObjectMetadata())
                .getMetadata().getContentLength();
    }
}
