package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.vfs.io.ByteBufferReadableChannel;
import cc.whohow.vfs.io.ProgressMonitorInputStream;
import cc.whohow.vfs.io.WritableChannel;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.AppendObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * OSS写入流，通过appendObject实现
 */
public class AliyunOSSWritableChannel extends WritableChannel {
    private final OSS oss;
    private final String bucketName;
    private final String key;
    private long position;

    public AliyunOSSWritableChannel(OSS oss, String bucketName, String key) {
        this(oss, bucketName, key, 0L);
    }

    public AliyunOSSWritableChannel(OSS oss, String bucketName, String key, long position) {
        this.oss = oss;
        this.bucketName = bucketName;
        this.key = key;
        this.position = position;
    }

    public OSS getOSS() {
        return oss;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getKey() {
        return key;
    }

    public long getPosition() {
        return position;
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
        position = oss.appendObject(
                new AppendObjectRequest(bucketName, key, new ByteArrayInputStream(b, off, len))
                        .withPosition(position)).getNextPosition();
    }

    @Override
    public int writeAll(ByteBuffer buffer) throws IOException {
        if (position != 0) {
            throw new IllegalStateException("position: " + position);
        }
        int n = buffer.remaining();
        oss.putObject(getBucketName(), getKey(), new ByteBufferReadableChannel(buffer));
        return n;
    }

    @Override
    public long transferFrom(InputStream stream) throws IOException {
        if (position != 0) {
            throw new IllegalStateException("position: " + position);
        }
        ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(stream);
        oss.putObject(getBucketName(), getKey(), monitor);
        return monitor.getPosition();
    }

    @Override
    public boolean isOpen() {
        return true;
    }
}
