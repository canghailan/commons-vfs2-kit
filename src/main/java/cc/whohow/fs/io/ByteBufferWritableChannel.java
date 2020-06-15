package cc.whohow.fs.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

public class ByteBufferWritableChannel extends OutputStream implements WritableByteChannel {
    protected Consumer<ByteBuffer> onClose;
    protected ByteBuffer buffer;

    public ByteBufferWritableChannel() {
        this(32);
    }

    public ByteBufferWritableChannel(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }

    public ByteBuffer getByteBuffer() {
        ByteBuffer copy = buffer.duplicate();
        copy.flip();
        return copy;
    }

    public ByteBufferWritableChannel onClose(Consumer<ByteBuffer> onClose) {
        this.onClose = onClose;
        return this;
    }

    private synchronized void ensureCapacity(int n) {
        if (buffer.remaining() < n) {
            int newCapacity = buffer.capacity() + n;
            if (newCapacity < buffer.capacity() * 2) {
                newCapacity = buffer.capacity() * 2;
            }
            buffer = ByteBuffers.resize(buffer, newCapacity);
        }
    }

    public synchronized int write(ByteBuffer b) {
        int n = b.remaining();
        ensureCapacity(n);
        buffer.put(b);
        return n;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        ensureCapacity(len);
        buffer.put(b, off, len);
    }

    @Override
    public synchronized void write(int b) {
        ensureCapacity(1);
        buffer.put((byte) b);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        if (onClose != null) {
            onClose.accept(getByteBuffer());
        }
    }
}
