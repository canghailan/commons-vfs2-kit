package cc.whohow.vfs.type;

import cc.whohow.vfs.io.ByteBufferReadableChannel;
import cc.whohow.vfs.io.ByteBufferWritableChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface DataType<T> {
    T deserialize(InputStream stream) throws IOException;

    void serialize(OutputStream stream, T value) throws IOException;

    default T deserialize(ByteBuffer buffer) throws IOException {
        return deserialize((InputStream) new ByteBufferReadableChannel(buffer));
    }

    default ByteBuffer serialize(T value) throws IOException {
        ByteBufferWritableChannel buffer = new ByteBufferWritableChannel();
        serialize((OutputStream) buffer, value);
        return buffer.getByteBuffer();
    }

    default T deserialize(ReadableByteChannel channel) throws IOException {
        return deserialize(Channels.newInputStream(channel));
    }

    default void serialize(WritableByteChannel channel, T value) throws IOException {
        serialize(Channels.newOutputStream(channel), value);
    }
}
