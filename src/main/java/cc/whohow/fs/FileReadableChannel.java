package cc.whohow.fs;

import cc.whohow.fs.util.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.util.Optional;

public interface FileReadableChannel extends ReadableByteChannel {
    /**
     * 返回值小于、等于0时表示大小未知
     *
     * @see java.nio.channels.FileChannel#size()
     */
    long size();

    /**
     * 返回相关文件对象属性，可选操作
     */
    default Optional<FileAttributes> readFileAttributes() {
        return Optional.empty();
    }

    /**
     * 请勿同时操作Channel、Stream
     */
    default InputStream stream() {
        return Channels.newInputStream(this);
    }

    /**
     * Java9InputStream
     *
     * @see Files#readAllBytes(java.nio.file.Path)
     */
    default ByteBuffer readAllBytes() throws IOException {
        return IO.read(stream());
    }

    /**
     * Java9InputStream
     */
    default long transferTo(OutputStream stream) throws IOException {
        return IO.copy(stream(), stream);
    }

    default long transferTo(WritableByteChannel channel) throws IOException {
        return IO.copy(this, channel);
    }

    default long transferTo(FileWritableChannel channel) throws IOException {
        return transferTo((WritableByteChannel) channel);
    }
}
