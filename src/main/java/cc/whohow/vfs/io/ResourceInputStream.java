package cc.whohow.vfs.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class ResourceInputStream extends InputStream {
    private final Closeable resource;
    private final InputStream stream;

    public ResourceInputStream(Closeable resource, InputStream stream) {
        this.resource = resource;
        this.stream = stream;
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
    public void close() throws IOException {
        try {
            stream.close();
        } finally {
            resource.close();
        }
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}
