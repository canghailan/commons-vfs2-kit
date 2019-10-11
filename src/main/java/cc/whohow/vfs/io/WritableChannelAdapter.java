package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.OutputStream;

public class WritableChannelAdapter extends WritableChannel {
    protected final OutputStream stream;
    protected volatile boolean open;

    public WritableChannelAdapter(OutputStream stream) {
        this.stream = stream;
        this.open = true;
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
            open =false;
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }
}
