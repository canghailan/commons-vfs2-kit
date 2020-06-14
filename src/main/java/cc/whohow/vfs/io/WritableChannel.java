package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public abstract class WritableChannel extends OutputStream implements WritableByteChannel {
    @Override
    public int write(ByteBuffer src) throws IOException {
        return IO.write((WritableByteChannel) this, src);
    }

    public int writeAll(ByteBuffer buffer) throws IOException {
        return write(buffer);
    }

    public long transferFrom(InputStream stream) throws IOException {
        return IO.copy(stream, this);
    }
}
