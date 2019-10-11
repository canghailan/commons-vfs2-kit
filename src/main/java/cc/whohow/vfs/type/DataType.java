package cc.whohow.vfs.type;

import cc.whohow.vfs.io.*;

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
        return deserialize(new ByteBufferReadableChannel(buffer));
    }

    default ByteBuffer serialize(T value) throws IOException {
        ByteBufferWritableChannel buffer = new ByteBufferWritableChannel();
        serialize(buffer, value);
        return buffer.getByteBuffer();
    }

    default T deserialize(ReadableChannel channel) throws IOException {
        return deserialize((InputStream) channel);
    }

    default void serialize(WritableChannel channel, T value) throws IOException {
        serialize((OutputStream) channel, value);
    }
}
