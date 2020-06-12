package cc.whohow.vfs.provider.qcloud.cos;

import cc.whohow.fs.channel.ByteBufferFileReadableChannel;
import cc.whohow.vfs.io.ProgressMonitorInputStream;
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
    private final int bufferSize;
    private long position;

    private String uploadId;
    private List<PartETag> partETags;
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
        this.bufferSize = bufferSize;
        this.position = 0;
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
        if (len == 0) {
            return;
        }
        if (uploadId == null) {
            uploadId = cos.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key))
                    .getUploadId();
            partETags = new ArrayList<>();
            buffer = new byte[bufferSize];
            length = 0;
        }

        if (len < buffer.length) {
            int r = buffer.length - length;
            if (r > len) {
                writeBuffer(b, off, len);
            } else if (r == len) {
                writeBuffer(b, off, len);
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

        position += len;
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
        if (uploadId != null) {
            cos.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadId, partETags));
        }
        if (position == 0) {
            writeAll(ByteBuffer.allocate(0));
        }
    }

    private synchronized void writeBuffer(byte[] b, int off, int len) {
        System.arraycopy(b, off, buffer, length, len);
        length += len;
    }

    private synchronized void flushBuffer(boolean close) {
        upload(buffer, 0, length, close);
        length = 0;
    }

    private synchronized void upload(byte[] buffer, int offset, int length, boolean last) {
        int partNumber = partETags.size() + 1;
        UploadPartResult result = cos.uploadPart(new UploadPartRequest()
                .withBucketName(bucketName)
                .withKey(key)
                .withUploadId(uploadId)
                .withPartNumber(partNumber)
                .withInputStream(new ByteArrayInputStream(buffer, offset, length))
                .withPartSize(length)
                .withLastPart(last)
        );
        partETags.add(new PartETag(partNumber, result.getETag()));
    }

    @Override
    public synchronized int writeAll(ByteBuffer buffer) throws IOException {
        if (position > 0) {
            throw new IllegalStateException("position: " + position);
        }
        position = buffer.remaining();
        cos.putObject(bucketName, key, new ByteBufferFileReadableChannel(buffer), new ObjectMetadata());
        return (int) position;
    }

    @Override
    public synchronized long transferFrom(InputStream stream) throws IOException {
        if (position > 0) {
            throw new IllegalStateException("position: " + position);
        }
        ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(stream);
        cos.putObject(bucketName, key, monitor, new ObjectMetadata());
        return position = monitor.getPosition();
    }
}
