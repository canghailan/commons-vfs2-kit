package cc.whohow.fs;

import cc.whohow.vfs.io.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;

public interface FileReadableChannel extends ReadableByteChannel {
    default InputStream stream() {
        return Channels.newInputStream(this);
    }

    /**
     * Java9InputStream
     *
     * @see Files#readAllBytes(java.nio.file.Path)
     */
    default ByteBuffer readAllBytes() throws IOException {
        return IO.read(stream());
    }

    /**
     * Java9InputStream
     */
    default long transferTo(OutputStream stream) throws IOException {
        return IO.copy(stream(), stream);
    }

    default long transferTo(WritableByteChannel channel) throws IOException {
        return IO.copy(this, channel);
    }

    default long transferTo(FileWritableChannel channel) throws IOException {
        return transferTo((WritableByteChannel) channel);
    }
}
