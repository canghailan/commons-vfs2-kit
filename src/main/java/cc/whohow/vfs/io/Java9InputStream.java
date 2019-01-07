package cc.whohow.vfs.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * InputStream工具
 */
public class Java9InputStream extends FilterInputStream implements ReadableByteChannel {
    protected static final int BUFFER_SIZE = 8 * 1024; // 8K

    protected volatile boolean open;

    public Java9InputStream(InputStream in) {
        super(in);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            open = false;
        }
    }

    public byte[] readAllBytes() throws IOException {
        ByteBuffer buffer = readAllBytes(BUFFER_SIZE);
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
            int n = read(buffer, offset, length);
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
            int n = read(buffer, offset, length);
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
        return transferTo(out, BUFFER_SIZE);
    }

    public long transferTo(OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long transferred = 0L;
        while (true) {
            int n = read(buffer);
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
            int n = read(dst.array(), dst.arrayOffset() + dst.position(), dst.remaining());
            dst.position(dst.position() + n);
            return n;
        } else {
            int length = dst.remaining();
            if (length > BUFFER_SIZE) {
                length = BUFFER_SIZE;
            }
            byte[] buffer = new byte[length];
            int n = read(buffer);
            dst.put(buffer, 0, n);
            return n;
        }
    }
}