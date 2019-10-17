package cc.whohow.vfs.serialize;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextSerializer implements Serializer<String> {
    private static final TextSerializer UTF_8 = new TextSerializer(StandardCharsets.UTF_8);
    protected final Charset charset;

    public TextSerializer(Charset charset) {
        this.charset = charset;
    }

    public static final TextSerializer utf8() {
        return UTF_8;
    }

    @Override
    public String deserialize(ByteBuffer buffer) {
        return charset.decode(buffer).toString();
    }

    @Override
    public ByteBuffer serialize(String value) {
        return (value == null || value.isEmpty()) ? ByteBuffer.allocate(0) : charset.encode(value);
    }

    @Override
    public String deserialize(InputStream stream) throws IOException {
        return deserialize(IO.read(stream));
    }

    @Override
    public void serialize(OutputStream stream, String value) throws IOException {
        IO.write(stream, serialize(value));
    }

    @Override
    public String deserialize(ReadableChannel channel) throws IOException {
        return deserialize(channel.readAll());
    }

    @Override
    public void serialize(WritableChannel channel, String value) throws IOException {
        channel.writeAll(serialize(value));
    }
}
