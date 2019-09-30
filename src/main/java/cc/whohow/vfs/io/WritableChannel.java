package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

public abstract class WritableChannel extends OutputStream implements WritableByteChannel {
    public long transferFrom(ReadableChannel channel) throws IOException {
        return transferFrom(channel, IO.BUFFER_SIZE);
    }

    public long transferFrom(ReadableChannel channel, int bufferSize) throws IOException {
        return IO.transfer(channel, this, bufferSize);
    }
}
