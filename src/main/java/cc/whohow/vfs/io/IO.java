package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

public class IO {
    public static final int BUFFER_SIZE = 8 * 1024;

    public static byte[] getByteArray(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            if (buffer.arrayOffset() == 0 && buffer.remaining() == buffer.capacity()) {
                return buffer.array();
            }
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public static long transfer(InputStream input, OutputStream output) throws IOException {
        return transfer(input, output, BUFFER_SIZE);
    }

    public static long transfer(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long transferred = 0L;
        while (true) {
            int n = input.read(buffer);
            if (n < 0) {
                return transferred;
            } else if (n > 0) {
                output.write(buffer, 0, n);
                transferred += n;
            }
        }
    }

    public static ByteBuffer read(InputStream input) throws IOException {
        return read(input, BUFFER_SIZE);
    }

    public static ByteBuffer read(InputStream input, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int offset = 0;
        int length = buffer.length;
        while (true) {
            int n = input.read(buffer, offset, length);
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

    public static int read(InputStream input, ByteBuffer buffer) throws IOException {
        if (!buffer.hasRemaining()) {
            return 0;
        }
        if (buffer.hasArray()) {
            int n = input.read(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
            if (n > 0) {
                buffer.position(buffer.position() + n);
            }
            return n;
        } else {
            int n = buffer.remaining();
            while (buffer.hasRemaining()) {
                int b = input.read();
                if (b < 0) {
                    if (buffer.remaining() == n) {
                        return b;
                    }
                    break;
                }
                buffer.put((byte) b);
            }
            return n - buffer.remaining();
        }
    }

    public static int write(OutputStream stream, ByteBuffer buffer) throws IOException {
        if (!buffer.hasRemaining()) {
            return 0;
        }
        int n = buffer.remaining();
        if (buffer.hasArray()) {
            stream.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
            buffer.position(buffer.limit());
        } else {
            while (buffer.hasRemaining()) {
                stream.write(buffer.get());
            }
        }
        return n;
    }

    public static void close(AutoCloseable... closeable) {
        for (AutoCloseable c : closeable) {
            close(c);
        }
    }

    public static void close(Collection<? extends AutoCloseable> closeable) {
        for (AutoCloseable c : closeable) {
            close(c);
        }
    }

    public static void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignore) {
        }
    }
}
