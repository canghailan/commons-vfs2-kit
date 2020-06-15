package cc.whohow.fs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.util.Arrays;

public class IO {
    public static final int BUFFER_SIZE = 8 * 1024;
    public static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    public static long copy(ReadableByteChannel input, WritableByteChannel output) throws IOException {
        return copy(input, output, BUFFER_SIZE);
    }

    /**
     * @see ByteBuffer#compact()
     */
    public static long copy(ReadableByteChannel input, WritableByteChannel output, int bufferSize) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        long n = 0;
        while (input.read(buffer) >= 0 || buffer.position() != 0) {
            buffer.flip();
            n += output.write(buffer);
            buffer.compact();
        }
        return n;
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, BUFFER_SIZE);
    }

    /**
     * @see Files#copy(java.io.InputStream, java.io.OutputStream)
     */
    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        long nread = 0L;
        byte[] buf = new byte[bufferSize];
        int n;
        while ((n = input.read(buf)) > 0) {
            output.write(buf, 0, n);
            nread += n;
        }
        output.flush();
        return nread;
    }

    public static ByteBuffer read(InputStream input) throws IOException {
        return read(input, BUFFER_SIZE);
    }

    /**
     * @see Files#read(java.io.InputStream, int)
     * @see Files#readAllBytes(java.nio.file.Path)
     */
    public static ByteBuffer read(InputStream input, int bufferSize) throws IOException {
        int capacity = bufferSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (; ; ) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = input.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = input.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte) n;
        }
        return ByteBuffer.wrap(buf, 0, nread);
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
                    return b;
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

    public static int write(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
        int n = buffer.remaining();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        return n;
    }
}
