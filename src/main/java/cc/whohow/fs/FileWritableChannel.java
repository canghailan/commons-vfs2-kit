package cc.whohow.fs;

import cc.whohow.fs.util.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface FileWritableChannel extends WritableByteChannel {
    default OutputStream stream() {
        return Channels.newOutputStream(this);
    }

    default long transferFrom(InputStream stream) throws IOException {
        return IO.copy(stream, stream());
    }

    default long transferFrom(ReadableByteChannel channel) throws IOException {
        return IO.copy(channel, this);
    }

    default long transferFrom(FileReadableChannel channel) throws IOException {
        return transferFrom((ReadableByteChannel) channel);
    }

    void overwrite(ByteBuffer bytes) throws IOException;
}
