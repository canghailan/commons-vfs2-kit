package cc.whohow.fs.util;

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
    public long size() {
        return buffer.limit();
    }

    @Override
    public InputStream stream() {
        return this;
    }

    @Override
    public ByteBuffer readAll() throws IOException {
        return buffer;
    }

    @Override
    public synchronized long transferTo(OutputStream stream) throws IOException {
        return IO.write(stream, buffer);
    }

    @Override
    public synchronized long transferTo(WritableByteChannel channel) throws IOException {
        return IO.write(channel, buffer);
    }

    @Override
    public long transferTo(FileWritableChannel channel) throws IOException {
        return transferTo((WritableByteChannel) channel);
    }
}
