package cc.whohow.fs.util;

import cc.whohow.fs.FileWritableChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class FileWritableStream extends OutputStream implements FileWritableChannel {
    protected final OutputStream stream;
    protected final Runnable onClose;
    protected volatile boolean open;

    public FileWritableStream(OutputStream stream) {
        this(stream, null);
    }

    public FileWritableStream(OutputStream stream, Runnable onClose) {
        this.stream = stream;
        this.onClose = onClose;
        this.open = true;
    }

    @Override
    public void write(int b) throws IOException {
        if (open) {
            stream.write(b);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (open) {
            stream.write(b);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (open) {
            stream.write(b, off, len);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public synchronized void close() throws IOException {
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
    public int write(ByteBuffer src) throws IOException {
        if (open) {
            return IO.write(stream, src);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public synchronized void overwrite(ByteBuffer bytes) throws IOException {
        write(bytes);
        open = false;
    }
}
