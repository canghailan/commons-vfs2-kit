package cc.whohow.vfs.type;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextType implements DataType<String> {
    private static final TextType UTF_8 = new TextType(StandardCharsets.UTF_8);

    public static final TextType utf8() {
        return UTF_8;
    }

    protected final Charset charset;

    public TextType(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String deserialize(ByteBuffer buffer) {
        return charset.decode(buffer).toString();
    }

    @Override
    public ByteBuffer serialize(String value) {
        return charset.encode(value);
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
