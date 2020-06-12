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
    URI getUri();

    boolean exists();

    FileAttributes readAttributes();

    FileReadableChannel newReadableChannel();

    FileWritableChannel newWritableChannel();

    void delete();

    default ByteBuffer read() {
        try (FileReadableChannel readableChannel = newReadableChannel()) {
            return readableChannel.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default void write(ByteBuffer content) {
        try (FileWritableChannel writableChannel = newWritableChannel()) {
            writableChannel.overwrite(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default String read(Charset charset) {
        return charset.decode(read()).toString();
    }

    default void write(CharSequence content, Charset charset) {
        write(charset.encode(CharBuffer.wrap(content)));
    }

    default String readUtf8() {
        return read(StandardCharsets.UTF_8);
    }

    default void writeUtf8(CharSequence content) {
        write(content, StandardCharsets.UTF_8);
    }
}
