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
import java.nio.file.Files;
import java.util.Optional;

/**
 * 文件读通道
 */
public interface FileReadableChannel extends ReadableByteChannel {
    /**
     * 返回值小于、等于0时表示大小未知
     *
     * @see java.nio.channels.FileChannel#size()
     */
    long size();

    /**
     * 返回相关文件属性，可选操作（如果不需要额外IO，则返回）
     */
    default Optional<FileAttributes> readFileAttributes() {
        return Optional.empty();
    }

    /**
     * 转为stream，请勿同时操作Channel、stream，不保证同时操作的正确性
     */
    default InputStream stream() {
        return Channels.newInputStream(this);
    }

    /**
     * 读取文件所有内容
     *
     * @see Files#readAllBytes(java.nio.file.Path)
     */
    default ByteBuffer readAll() throws IOException {
        return IO.read(stream());
    }

    /**
     * 读取文件所有内容，并写入到输出流中
     *
     * @see FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)
     */
    default long transferTo(OutputStream stream) throws IOException {
        return IO.copy(stream(), stream);
    }

    /**
     * 读取文件所有内容，并写入到写通道中
     *
     * @see FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)
     */
    default long transferTo(WritableByteChannel channel) throws IOException {
        return IO.copy(this, channel);
    }

    /**
     * 读取文件所有内容，并写入到写通道中
     *
     * @see FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)
     */
    default long transferTo(FileWritableChannel channel) throws IOException {
        return transferTo((WritableByteChannel) channel);
    }
}
