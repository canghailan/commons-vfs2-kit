package cc.whohow.fs.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressMonitorInputStream extends FilterInputStream {
    protected final AtomicLong position = new AtomicLong();
    protected volatile long mark;

    public ProgressMonitorInputStream(InputStream in) {
        super(in);
    }

    public ProgressMonitorInputStream(InputStream in, long position) {
        super(in);
        this.position.set(position);
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b >= 0) {
            position.getAndIncrement();
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int bytes = in.read(b);
        if (bytes > 0) {
            position.getAndAdd(bytes);
        }
        return bytes;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytes = in.read(b, off, len);
        if (bytes > 0) {
            position.getAndAdd(bytes);
        }
        return bytes;
    }

    @Override
    public long skip(long n) throws IOException {
        long bytes = in.skip(n);
        position.getAndAdd(bytes);
        return bytes;
    }

    @Override
    public synchronized void mark(int limit) {
        in.mark(limit);
        mark = position.get();
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        position.set(mark);
    }

    public long getPosition() {
        return position.get();
    }
}
