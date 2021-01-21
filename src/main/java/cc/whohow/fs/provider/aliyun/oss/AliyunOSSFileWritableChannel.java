package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.util.ByteBufferReadableChannel;
import cc.whohow.fs.util.ProgressMonitorInputStream;
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
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;
    private final OSS oss;
    private final String bucketName;
    private final String key;
    // 缓冲区大小
    private final int bufferSize;

    // 读写缓冲区，参考Netty ByteBuf
    // 0 <= readIndex <= writeIndex < buffer.length
    private byte[] buffer;
    private int readIndex;
    private int writeIndex;

    private long position;

    public AliyunOSSFileWritableChannel(OSS oss, String bucketName, String key) {
        this(oss, bucketName, key, 0L);
    }

    public AliyunOSSFileWritableChannel(OSS oss, String bucketName, String key, long position) {
        this(oss, bucketName, key, BUFFER_SIZE, position);
    }

    public AliyunOSSFileWritableChannel(OSS oss, String bucketName, String key, int bufferSize, long position) {
        this.oss = oss;
        this.bucketName = bucketName;
        this.key = key;
        this.bufferSize = bufferSize;
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
    public synchronized void write(int b) throws IOException {
        initializeBuffer();
        if (writableBytes() < 1) {
            // 缓冲区不足
            // 提交缓冲区
            flush();
        }
        buffer[writeIndex++] = (byte) b;
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

        initializeBuffer();
        if (len < buffer.length) {
            // 小数据块写入
            if (writableBytes() < len) {
                // 缓冲区不足
                // 提交缓冲区
                flush();
                if (len * 2 > buffer.length) {
                    // 较大数据块，直接提交
                    flush(b, off, len);
                } else {
                    // 较小数据块，写入缓冲区
                    append(b, off, len);
                }
            } else {
                // 缓冲区足够，写入缓冲区
                append(b, off, len);
            }
        } else {
            // 大数据块写入
            // 提交缓冲区
            flush();
            // 提交数据块
            flush(b, off, len);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (position == 0) {
            if (buffer != null) {
                // 小文件优化，如果没有调用过appendObject，直接putObject
                overwrite(ByteBuffer.wrap(buffer, readIndex, readableBytes()));
            } else {
                // 空文件
                log.debug("empty file: oss://{}/{}", bucketName, key);
                overwrite(ByteBuffer.allocate(0));
            }
        } else {
            flush();
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {
        if (!src.hasRemaining()) {
            return 0;
        }
        if (src.hasArray()) {
            // 如果有底层数组，复用write(byte[], int, int)
            int len = src.remaining();
            write(src.array(), src.arrayOffset() + src.position(), len);
            src.position(src.position() + len);
            return len;
        }

        // 没有底层数组
        initializeBuffer();
        int len = src.remaining();
        if (writableBytes() < len) {
            // 缓冲区不够
            // 提交缓冲区
            flush();
            if (len * 2 > buffer.length) {
                // 较大数据块，直接提交
                flush(src);
            } else {
                // 较小数据块，写入缓冲区
                append(src);
            }
        } else {
            // 缓冲区足够
            append(src);
        }
        return len;
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

    @Override
    public synchronized void flush() throws IOException {
        if (buffer != null) {
            int length = readableBytes();
            if (length > 0) {
                flush(buffer, readIndex, length);
                readIndex = 0;
                writeIndex = 0;
            }
        }
    }

    protected synchronized void flush(byte[] buf, int off, int len) {
        log.trace("appendObject: oss://{}/{}?position={}&length={}", bucketName, key, position, len);
        flush(new ByteArrayInputStream(buf, off, len));
    }

    protected synchronized void flush(ByteBuffer buf) {
        log.trace("appendObject: oss://{}/{}?position={}&length={}", bucketName, key, position, buf.remaining());
        flush(new ByteBufferReadableChannel(buf));
    }

    protected synchronized void flush(InputStream stream) {
        if (position == 0) {
            // 删除已有文件
            log.trace("deleteObject: oss://{}/{}", bucketName, key);
            oss.deleteObject(bucketName, key);
        }
        position = oss.appendObject(
                new AppendObjectRequest(bucketName, key, stream)
                        .withPosition(position)).getNextPosition();
    }

    /**
     * 可写入缓冲区大小
     */
    protected synchronized int writableBytes() {
        return buffer.length - writeIndex;
    }

    /**
     * 可读取缓冲区大小
     */
    protected synchronized int readableBytes() {
        return writeIndex - readIndex;
    }

    protected synchronized void append(byte[] buf, int off, int len) {
        System.arraycopy(buf, off, buffer, writeIndex, len);
        writeIndex += len;
    }

    protected synchronized void append(ByteBuffer buf) {
        int len = buf.remaining();
        buf.get(buffer, writeIndex, len);
        writeIndex += len;
    }

    protected synchronized void initializeBuffer() {
        if (buffer == null) {
            buffer = new byte[bufferSize];
            readIndex = 0;
            writeIndex = 0;
        }
    }
}
