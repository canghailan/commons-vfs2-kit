package cc.whohow.vfs.type;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BinaryType implements DataType<ByteBuffer> {
    private static final BinaryType INSTANCE = new BinaryType();

    public static BinaryType get() {
        return INSTANCE;
    }

    private BinaryType() {
    }

    @Override
    public ByteBuffer deserialize(InputStream stream) throws IOException {
        return IO.read(stream);
    }

    @Override
    public void serialize(OutputStream stream, ByteBuffer value) throws IOException {
        IO.write(stream, value);
    }

    @Override
    public ByteBuffer deserialize(ByteBuffer buffer) {
        return buffer;
    }

    @Override
    public ByteBuffer serialize(ByteBuffer value) {
        return value;
    }

    @Override
    public ByteBuffer deserialize(ReadableChannel channel) throws IOException {
        return channel.readAll();
    }

    @Override
    public void serialize(WritableChannel channel, ByteBuffer value) throws IOException {
        channel.writeAll(value);
    }
}
