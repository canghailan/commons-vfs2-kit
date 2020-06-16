package cc.whohow.fs.io;

import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class ByteBufferFileReadableChannel extends ByteBufferReadableChannel implements FileReadableChannel {
    public ByteBufferFileReadableChannel(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public InputStream stream() {
        return this;
    }

    @Override
    public ByteBuffer readAllBytes() throws IOException {
        return buffer;
    }

    @Override
    public long transferTo(OutputStream stream) throws IOException {
        return IO.write(stream, buffer);
    }

    @Override
    public long transferTo(WritableByteChannel channel) throws IOException {
        return IO.write(channel, buffer);
    }

    @Override
    public long transferTo(FileWritableChannel channel) throws IOException {
        return transferTo((WritableByteChannel) channel);
    }
}
