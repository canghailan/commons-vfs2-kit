package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public abstract class ReadableChannel extends InputStream implements ReadableByteChannel {
    public ByteBuffer readAll() throws IOException {
        return IO.read(this);
    }

    public long transferTo(WritableChannel channel) throws IOException {
        return transferTo(channel, IO.BUFFER_SIZE);
    }

    public long transferTo(WritableChannel channel, int bufferSize) throws IOException {
        return IO.transfer(this, channel, bufferSize);
    }
}
