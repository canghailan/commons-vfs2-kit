package cc.whohow.vfs.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class ResourceOutputStream extends OutputStream {
    private final Closeable resource;
    private final OutputStream stream;

    public ResourceOutputStream(Closeable resource, OutputStream stream) {
        this.resource = resource;
        this.stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        stream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            stream.close();
        } finally {
            resource.close();
        }
    }
}
