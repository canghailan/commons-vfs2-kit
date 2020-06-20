package cc.whohow.fs;

import cc.whohow.fs.util.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 文件写通道
 */
public interface FileWritableChannel extends WritableByteChannel {
    /**
     * 请勿同时操作Channel、stream
     */
    default OutputStream stream() {
        return Channels.newOutputStream(this);
    }

    /**
     * 读取输入流，并覆盖写入到文件中
     *
     * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
     */
    default long transferFrom(InputStream stream) throws IOException {
        return IO.copy(stream, stream());
    }

    /**
     * 读取读通道，并覆盖写入到文件中
     *
     * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
     */
    default long transferFrom(ReadableByteChannel channel) throws IOException {
        return IO.copy(channel, this);
    }

    /**
     * 读取读通道，并覆盖写入到文件中
     *
     * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
     */
    default long transferFrom(FileReadableChannel channel) throws IOException {
        return transferFrom((ReadableByteChannel) channel);
    }

    /**
     * 覆盖写入所有字节
     */
    void overwrite(ByteBuffer bytes) throws IOException;
}
