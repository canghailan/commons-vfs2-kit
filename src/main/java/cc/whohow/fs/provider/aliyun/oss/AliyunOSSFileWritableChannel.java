package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.io.ByteBufferReadableChannel;
import cc.whohow.vfs.io.ProgressMonitorInputStream;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.AppendObjectRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * OSS写入流，通过appendObject实现
 */
public class AliyunOSSFileWritableChannel extends OutputStream implements FileWritableChannel {
    private static final Logger log = LogManager.getLogger(AliyunOSSFileWritableChannel.class);
    private final OSS oss;
    private final String bucketName;
    private final String key;
    private long position;

    public AliyunOSSFileWritableChannel(OSS oss, String bucketName, String key) {
        this(oss, bucketName, key, 0L);
    }

    public AliyunOSSFileWritableChannel(OSS oss, String bucketName, String key, long position) {
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
        if (len == 0) {
            return;
        }
        log.trace("appendObject: oss://{}/{} {}", bucketName, key, position);
        position = oss.appendObject(
                new AppendObjectRequest(bucketName, key, new ByteArrayInputStream(b, off, len))
                        .withPosition(position)).getNextPosition();
    }

    @Override
    public synchronized void close() throws IOException {
        if (position == 0) {
            overwrite(ByteBuffer.allocate(0));
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {
        int n = src.remaining();
        if (n == 0) {
            return 0;
        }
        log.trace("appendObject: oss://{}/{} {}", bucketName, key, position);
        position = oss.appendObject(
                new AppendObjectRequest(bucketName, key, new ByteBufferReadableChannel(src))
                        .withPosition(position)).getNextPosition();
        return n;
    }

    @Override
    public OutputStream stream() {
        return this;
    }

    @Override
    public synchronized void overwrite(ByteBuffer buffer) throws IOException {
        if (position > 0) {
            throw new IllegalStateException("position: " + position);
        }
        log.trace("putObject: oss://{}/{}", bucketName, key);
        position = buffer.remaining();
        oss.putObject(bucketName, key, new ByteBufferReadableChannel(buffer));
    }

    @Override
    public synchronized long transferFrom(InputStream stream) throws IOException {
        if (position > 0) {
            throw new IllegalStateException("position: " + position);
        }
        log.trace("putObject: oss://{}/{}", bucketName, key);
        ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(stream);
        oss.putObject(bucketName, key, monitor);
        return position = monitor.getPosition();
    }

    @Override
    public long transferFrom(ReadableByteChannel channel) throws IOException {
        return transferFrom(Channels.newInputStream(channel));
    }

    @Override
    public long transferFrom(FileReadableChannel channel) throws IOException {
        return transferFrom(channel.stream());
    }
}
