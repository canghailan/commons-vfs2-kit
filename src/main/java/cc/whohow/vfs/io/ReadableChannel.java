package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public abstract class ReadableChannel extends InputStream implements ReadableByteChannel {
    @Override
    public int read(ByteBuffer dst) throws IOException {
        return IO.read(this, dst);
    }

    public ByteBuffer readAll() throws IOException {
        return IO.read(this);
    }

    public long transferTo(OutputStream channel) throws IOException {
        return IO.transfer(this, channel);
    }
}
