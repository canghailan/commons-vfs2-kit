package cc.whohow.vfs.serialize;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.io.ByteBufferReadableChannel;
import cc.whohow.vfs.io.ByteBufferWritableChannel;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface Serializer<T> {
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

    default T deserialize(FileObject fileObject) throws IOException {
        try (FileContent content = fileObject.getContent()) {
            try (InputStream stream = content.getInputStream()) {
                return deserialize(stream);
            }
        }
    }

    default void serialize(FileObject fileObject, T value) throws IOException {
        try (FileContent content = fileObject.getContent()) {
            try (OutputStream stream = content.getOutputStream()) {
                serialize(stream, value);
            }
        }
    }

    default T deserialize(CloudFileObject fileObject) throws IOException {
        try (ReadableChannel channel = fileObject.getReadableChannel()) {
            return deserialize(channel);
        }
    }

    default void serialize(CloudFileObject fileObject, T value) throws IOException {
        try (WritableChannel channel = fileObject.getWritableChannel()) {
            serialize(channel, value);
        }
    }
}
