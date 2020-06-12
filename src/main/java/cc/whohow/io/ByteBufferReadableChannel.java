package cc.whohow.io;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ByteBufferReadableChannel extends InputStream implements ReadableByteChannel {
    protected ByteBuffer buffer;

    public ByteBufferReadableChannel(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {
        if (buffer.hasRemaining()) {
            if (len > buffer.remaining()) {
                len = buffer.remaining();
            }
            buffer.get(b, off, len);
            return len;
        }
        return -1;
    }

    @Override
    public synchronized long skip(long n) {
        if (n > buffer.remaining()) {
            n = buffer.remaining();
        }
        buffer.position(buffer.position() + (int) n);
        return n;
    }

    @Override
    public int available() {
        return buffer.remaining();
    }

    @Override
    public void mark(int limit) {
        buffer.mark();
    }

    @Override
    public synchronized void reset() {
        buffer.reset();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * @see java.io.ByteArrayInputStream#read()
     */
    @Override
    public synchronized int read() {
        return buffer.hasRemaining() ? buffer.get() & 0xff : -1;
    }

    @Override
    public synchronized int read(ByteBuffer dst) {
        if (buffer.hasRemaining()) {
            int n = dst.remaining();
            if (n < buffer.remaining()) {
                ByteBuffer src = buffer.duplicate();
                src.limit(src.position() + n);
                dst.put(src);
                buffer.position(buffer.position() + n);
            } else {
                n = buffer.remaining();
                dst.put(buffer);
            }
            return n;
        }
        return -1;
    }

    @Override
    public boolean isOpen() {
        return true;
    }
}
