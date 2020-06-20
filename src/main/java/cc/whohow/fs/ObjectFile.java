package cc.whohow.fs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 对象存储文件
 */
public interface ObjectFile {
    /**
     * 对象URI
     */
    URI getUri();

    /**
     * 是否存在
     */
    boolean exists();

    /**
     * 读取文件属性
     */
    FileAttributes readAttributes();

    /**
     * 文件读通道
     */
    FileReadableChannel newReadableChannel();

    /**
     * 文件写通道
     */
    FileWritableChannel newWritableChannel();

    /**
     * 删除
     */
    void delete();

    /**
     * 读取所有字节
     */
    default ByteBuffer read() {
        try (FileReadableChannel readableChannel = newReadableChannel()) {
            return readableChannel.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 写入所有字节
     */
    default void write(ByteBuffer content) {
        try (FileWritableChannel writableChannel = newWritableChannel()) {
            writableChannel.overwrite(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 读取所有文本
     */
    default String read(Charset charset) {
        return charset.decode(read()).toString();
    }

    /**
     * 写入所有文本
     */
    default void write(CharSequence content, Charset charset) {
        write(charset.encode(CharBuffer.wrap(content)));
    }

    /**
     * 读取所有文本（UTF8编码）
     */
    default String readUtf8() {
        return read(StandardCharsets.UTF_8);
    }

    /**
     * 写入所有文本（UTF8编码）
     */
    default void writeUtf8(CharSequence content) {
        write(content, StandardCharsets.UTF_8);
    }
}
