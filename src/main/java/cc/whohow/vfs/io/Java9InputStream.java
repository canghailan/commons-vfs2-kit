package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * InputStream工具
 */
public class Java9InputStream extends InputStream implements ReadableByteChannel {
    protected final InputStream delegate;
    protected boolean open;

    public Java9InputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } finally {
            open = false;
        }
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    public byte[] readAllBytes() throws IOException {
        ByteBuffer buffer = readAllBytes(8 * 1024);
        if (buffer.hasArray()) {
            if (buffer.arrayOffset() == 0 && buffer.remaining() == buffer.capacity()) {
                return buffer.array();
            }
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public ByteBuffer readAllBytes(int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int offset = 0;
        int length = buffer.length;
        while (true) {
            int n = delegate.read(buffer, offset, length);
            if (n < 0) {
                return ByteBuffer.wrap(buffer, 0, offset);
            } else if (n > 0) {
                offset += n;
                length -= n;
                if (length == 0) {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                    length = buffer.length - offset;
                }
            }
        }
    }

    public int readNBytes(byte[] buffer, int offset, int length) throws IOException {
        int bytes = 0;
        while (length > 0) {
            int n = delegate.read(buffer, offset, length);
            if (n < 0) {
                return bytes;
            } else if (n > 0) {
                bytes += n;
                offset += n;
                length -= n;
            }
        }
        return bytes;
    }

    public ByteBuffer readNBytes(int length) throws IOException {
        byte[] buffer = new byte[length];
        int n = readNBytes(buffer, 0, length);
        return ByteBuffer.wrap(buffer, 0, n);
    }

    public long transferTo(OutputStream out) throws IOException {
        return transferTo(out, 8 * 1024);
    }

    public long transferTo(OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long transferred = 0L;
        while (true) {
            int n = delegate.read(buffer);
            if (n < 0) {
                return transferred;
            } else if (n > 0) {
                out.write(buffer, 0, n);
                transferred += n;
            }
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (dst.hasArray()) {
            int n = delegate.read(dst.array(), dst.arrayOffset() + dst.position(), dst.remaining());
            dst.position(dst.position() + n);
            return n;
        } else {
            int length = dst.remaining();
            if (length > 8 * 1024) {
                length = 8 * 1024;
            }
            byte[] buffer = new byte[length];
            int n = delegate.read(buffer);
            dst.put(buffer, 0, n);
            return n;
        }
    }
}