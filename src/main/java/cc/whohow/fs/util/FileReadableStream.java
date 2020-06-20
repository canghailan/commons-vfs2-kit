package cc.whohow.fs.util;

import cc.whohow.fs.FileReadableChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class FileReadableStream extends InputStream implements FileReadableChannel {
    protected final InputStream stream;
    protected final Runnable onClose;
    protected volatile boolean open;

    public FileReadableStream(InputStream stream) {
        this(stream, null);
    }

    public FileReadableStream(InputStream stream, Runnable onClose) {
        this.stream = stream;
        this.onClose = onClose;
        this.open = true;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return stream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        try {
            stream.close();
        } finally {
            open = false;
            if (onClose != null) {
                onClose.run();
            }
        }
    }

    @Override
    public void mark(int limit) {
        stream.mark(limit);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return IO.read(stream, dst);
    }

    @Override
    public long size() {
        return -1;
    }

    @Override
    public InputStream stream() {
        return stream;
    }

    @Override
    public ByteBuffer readAllBytes() throws IOException {
        return IO.read(stream);
    }
}
