package cc.whohow.vfs.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ByteBufferChannel implements SeekableByteChannel {
    private byte[] buffer;
    private int offset;
    private int size;
    private int position;

    @Override
    public synchronized int read(ByteBuffer dst) throws IOException {
        if (position >= size) {
            return -1;
        }
        int n = Integer.min(dst.remaining(), size - position);
        dst.put(buffer, offset + position, n);
        position += n;
        return n;
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {
        int n = src.remaining();
        int newSize = size + n;
        int newPosition = position + n;
        if (newSize > buffer.length) {
            byte[] newBuffer = new byte[Integer.max(newSize, buffer.length * 2)];
            System.arraycopy(buffer, offset, newBuffer, 0, size);
            buffer = newBuffer;
            offset = 0;
        }
        if (newPosition > buffer.length) {
            System.arraycopy(buffer, offset, buffer, 0, size);
            offset = 0;
        }
        src.get(buffer, offset + position, n);
        size = newSize;
        position = newPosition;
        return n;
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public synchronized SeekableByteChannel position(long newPosition) throws IOException {
        position = (int) newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return size;
    }

    @Override
    public synchronized SeekableByteChannel truncate(long size) throws IOException {
        if (this.size > size) {
            this.size = (int) size;
        }
        if (this.position > this.size) {
            this.position = this.size;
        }
        return this;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }
}
