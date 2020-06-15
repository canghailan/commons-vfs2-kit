package cc.whohow.fs.channel;

import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.io.ByteBufferWritableChannel;
import cc.whohow.fs.io.ByteBuffers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;

public class ByteBufferFileWritableChannel extends ByteBufferWritableChannel implements FileWritableChannel {
    public ByteBufferFileWritableChannel() {
        super();
    }

    public ByteBufferFileWritableChannel(int size) {
        super(size);
    }

    @Override
    public ByteBufferFileWritableChannel onClose(Consumer<ByteBuffer> onClose) {
        super.onClose(onClose);
        return this;
    }

    @Override
    public OutputStream stream() {
        return this;
    }

    @Override
    public void overwrite(ByteBuffer bytes) throws IOException {
        this.buffer = bytes;
    }

    @Override
    public synchronized long transferFrom(InputStream stream) throws IOException {
        long n = 0;
        while (true) {
            if (!buffer.hasRemaining()) {
                buffer = ByteBuffers.resize(buffer, buffer.capacity() * 2);
            }
            int nr = stream.read(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
            if (nr < 0) {
                return n;
            }
            buffer.position(buffer.position() + nr);
            n += nr;
        }
    }

    @Override
    public long transferFrom(ReadableByteChannel channel) throws IOException {
        long n = 0;
        while (true) {
            if (!buffer.hasRemaining()) {
                buffer = ByteBuffers.resize(buffer, buffer.capacity() * 2);
            }
            int nr = channel.read(buffer);
            if (nr < 0) {
                return n;
            }
            n += nr;
        }
    }

    @Override
    public long transferFrom(FileReadableChannel channel) throws IOException {
        return transferFrom((ReadableByteChannel) channel);
    }
}
