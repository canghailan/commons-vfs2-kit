package cc.whohow.fs.provider.file;

import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.util.IO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LocalFileWritableChannel implements FileWritableChannel {
    private static final Logger log = LogManager.getLogger(LocalFileWritableChannel.class);
    protected final FileOutputStream stream;
    protected final FileChannel fileChannel;

    public LocalFileWritableChannel(FileOutputStream stream) {
        this.stream = stream;
        this.fileChannel = stream.getChannel();
    }

    @Override
    public OutputStream stream() {
        return stream;
    }

    @Override
    public long transferFrom(FileReadableChannel channel) throws IOException {
        long size = channel.size();
        if (size <= 0) {
            return IO.copy(channel, this);
        }
        long position = 0;
        while (position < size) {
            log.trace("FileChannel.transferFrom: {} {}", position, size - position);
            position += fileChannel.transferFrom(channel, position, size - position);
        }
        fileChannel.position(position);
        return position;
    }

    @Override
    public void overwrite(ByteBuffer bytes) throws IOException {
        fileChannel.truncate(0);
        IO.write(fileChannel, bytes);
        fileChannel.force(false);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return fileChannel.write(src);
    }

    @Override
    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
