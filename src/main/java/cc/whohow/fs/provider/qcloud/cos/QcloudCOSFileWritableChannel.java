package cc.whohow.fs.provider.qcloud.cos;

import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.io.ByteBufferReadableChannel;
import cc.whohow.fs.io.ProgressMonitorInputStream;
import com.qcloud.cos.COS;
import com.qcloud.cos.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * COS写入流，通过MultipartUpload实现
 */
public class QcloudCOSFileWritableChannel extends OutputStream implements FileWritableChannel {
    private static final Logger log = LogManager.getLogger(QcloudCOSFileWritableChannel.class);
    private static final int MIN_PART_SIZE = 1024 * 1024;

    private final COS cos;
    private final String bucketName;
    private final String key;
    // 缓冲区大小
    private final int bufferSize;

    // 分块上传ID、列表
    private String uploadId;
    private List<PartETag> parts;

    // 读写缓冲区，参考Netty ByteBuf
    // 0 <= readIndex <= writeIndex < buffer.length
    private byte[] buffer;
    private int readIndex;
    private int writeIndex;

    // 已上传大小
    private long position;


    public QcloudCOSFileWritableChannel(COS cos, String bucketName, String key) {
        // 默认2倍最小上传大小，防止内存数据多次移动
        this(cos, bucketName, key, 2 * MIN_PART_SIZE);
    }

    public QcloudCOSFileWritableChannel(COS cos, String bucketName, String key, int bufferSize) {
        if (bufferSize < MIN_PART_SIZE) {
            throw new IllegalArgumentException(bufferSize + " < " + MIN_PART_SIZE + "(MinBufferSize)");
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
    public synchronized void write(int b) throws IOException {
        initiateMultipartUpload();
        if (writableBytes() < 1) {
            // 缓冲区不足
            // 提交、压缩缓冲区
            flush();
            compact();
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

        // 初始化分块上传
        initiateMultipartUpload();
        if (len < buffer.length) {
            // 小数据块写入
            if (writableBytes() < len) {
                // 缓冲区不足
                // 提交、压缩缓冲区
                flush();
                compact();
            }
            // 缓冲区可写大小
            int n = writableBytes();
            if (n < len) {
                // 缓冲区不足写入全部数据块，填充剩余缓冲区
                append(b, off, n);
                // 缓冲区满，上传缓冲区数据块
                flush();
                // 写入剩余数据块到缓冲区
                append(b, off + n, len - n);
            } else {
                // 缓冲区足够写入全部数据块
                append(b, off, len);
            }
        } else {
            // 大数据块写入
            if (readableBytes() > 0) {
                // 有未上传缓冲区数据块
                // 提交、压缩缓冲区
                flush();
                compact();
            }
            if (readableBytes() > 0) {
                // 有未上传缓冲区数据块
                // 缓冲区可写大小
                int n = writableBytes();
                // 填充剩余缓冲区
                append(b, off, n);
                // 缓冲区满，上传缓冲区数据块
                flush();
                if ((len - n) > MIN_PART_SIZE && (len - n) * 2 > buffer.length) {
                    // 剩余数据块满足上传最小要求，较大数据块，直接上传
                    uploadPart(b, off + n, len - n, false);
                } else {
                    // 剩余数据块较小，写入到缓冲区
                    append(b, off + n, len - n);
                }
            } else {
                // 无未上传缓冲区数据块，直接上传分块
                uploadPart(b, off, len, false);
            }
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (uploadId != null) {
            // 如果有未提交缓冲区，提交缓冲区
            int length = readableBytes();
            if (length > 0) {
                uploadPart(buffer, readIndex, length, true);
            }
            log.trace("completeMultipartUpload: cos://{}/{}?uploadId={}&parts={}", bucketName, key, uploadId, parts.size());
            cos.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadId, parts));
        }
        if (position == 0) {
            // 空文件
            log.debug("empty file: cos://{}/{}", bucketName, key);
            overwrite(ByteBuffer.allocate(0));
        }
    }

    @Override
    public synchronized void overwrite(ByteBuffer bytes) throws IOException {
        if (position > 0) {
            throw new IllegalStateException("position: " + position);
        }
        log.trace("putObject: cos://{}/{}", bucketName, key);
        position = bytes.remaining();
        cos.putObject(bucketName, key, new ByteBufferReadableChannel(bytes), new ObjectMetadata());
    }

    @Override
    public synchronized long transferFrom(InputStream stream) throws IOException {
        if (position > 0) {
            throw new IllegalStateException("position: " + position);
        }
        log.debug("putObject: cos://{}/{}", bucketName, key);
        ProgressMonitorInputStream monitor = new ProgressMonitorInputStream(stream);
        cos.putObject(bucketName, key, monitor, new ObjectMetadata());
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
        initiateMultipartUpload();
        if (writableBytes() < src.remaining()) {
            // 缓冲区不够
            // 提交、压缩缓冲区
            flush();
            compact();
            if (readableBytes() > 0) {
                // 有未提交缓冲区，填充剩余缓冲区
                int len = Integer.min(writableBytes(), src.remaining());
                append(src, len);
                return len;
            } else {
                // 无未提交缓冲区
                int len = src.remaining();
                if (len > MIN_PART_SIZE && len * 2 > buffer.length) {
                    // 满足上传最小要求，较大数据块，直接提交
                    uploadPart(src);
                } else {
                    // 较小数据块，写入缓冲区
                    append(src);
                }
                return len;
            }
        } else {
            // 缓冲区足够
            int len = src.remaining();
            append(src);
            return len;
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (uploadId != null) {
            int length = readableBytes();
            if (length > MIN_PART_SIZE) {
                // 达到上传最小块要求
                uploadPart(buffer, readIndex, length, false);
                readIndex = 0;
                writeIndex = 0;
            }
        }
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

    /**
     * @see ByteBuffer#compact()
     */
    protected synchronized void compact() {
        int length = readableBytes();
        if (length > 0) {
            System.arraycopy(buffer, readIndex, buffer, 0, length);
            readIndex = 0;
            writeIndex = length;
        }
    }

    protected synchronized void append(byte[] buf, int off, int len) {
        System.arraycopy(buf, off, buffer, writeIndex, len);
        writeIndex += len;
    }

    protected synchronized void append(ByteBuffer src) {
        append(src, src.remaining());
    }

    protected synchronized void append(ByteBuffer src, int len) {
        src.get(buffer, writeIndex, len);
        writeIndex += len;
    }

    protected synchronized void uploadPart(ByteBuffer buf) {
        int length = buf.remaining();
        int partNumber = parts.size() + 1;
        log.trace("uploadPart: cos://{}/{}?uploadId={}&part={}&position={}&length={}", bucketName, key, uploadId, partNumber, position, length);
        UploadPartResult result = cos.uploadPart(new UploadPartRequest()
                .withBucketName(bucketName)
                .withKey(key)
                .withUploadId(uploadId)
                .withPartNumber(partNumber)
                .withInputStream(new ByteBufferReadableChannel(buf))
                .withPartSize(length)
                .withLastPart(false)
        );
        parts.add(result.getPartETag());
        position += length;
    }

    protected synchronized void uploadPart(byte[] buffer, int offset, int length, boolean last) {
        int partNumber = parts.size() + 1;
        log.trace("uploadPart: cos://{}/{}?uploadId={}&part={}&position={}&length={}", bucketName, key, uploadId, partNumber, position, length);
        UploadPartResult result = cos.uploadPart(new UploadPartRequest()
                .withBucketName(bucketName)
                .withKey(key)
                .withUploadId(uploadId)
                .withPartNumber(partNumber)
                .withInputStream(new ByteArrayInputStream(buffer, offset, length))
                .withPartSize(length)
                .withLastPart(last)
        );
        parts.add(result.getPartETag());
        position += length;
    }

    protected synchronized void initiateMultipartUpload() {
        if (uploadId == null) {
            // 初始化分块上传
            log.trace("initiateMultipartUpload: cos://{}/{}", bucketName, key);
            uploadId = cos.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key))
                    .getUploadId();
            log.debug("initiateMultipartUpload: cos://{}/{}?uploadId={}", bucketName, key, uploadId);
            parts = new ArrayList<>();
            buffer = new byte[bufferSize];
            readIndex = 0;
            writeIndex = 0;
        }
    }
}
