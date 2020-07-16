package cc.whohow.fs.provider.file;

import cc.whohow.fs.FileReadableChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class LocalFileReadableChannel implements FileReadableChannel {
    private static final Logger log = LogManager.getLogger(LocalFileReadableChannel.class);
    protected final FileInputStream stream;
    protected final FileChannel fileChannel;

    public LocalFileReadableChannel(FileInputStream stream) {
        this.stream = stream;
        this.fileChannel = stream.getChannel();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return fileChannel.read(dst);
    }

    @Override
    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public long size() {
        try {
            return fileChannel.size();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream stream() {
        return stream;
    }

    @Override
    public ByteBuffer readAll() throws IOException {
        long size = fileChannel.size();
//        log.trace("FileChannel.map: {} - {}", 0, size);
//        ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
//        fileChannel.position(size);
        if (size > Integer.MAX_VALUE) {
            throw new IllegalStateException("file too large");
        }
        ByteBuffer buffer = ByteBuffer.allocate((int) size);
        while (buffer.hasRemaining()) {
            if (fileChannel.read(buffer) < 0) {
                break;
            }
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public long transferTo(OutputStream stream) throws IOException {
        return transferTo(Channels.newChannel(stream));
    }

    @Override
    public long transferTo(WritableByteChannel channel) throws IOException {
        long size = fileChannel.size();
        long position = 0;
        while (position < size) {
            log.trace("FileChannel.transferTo: {} {}", position, size - position);
            position += fileChannel.transferTo(position, size - position, channel);
        }
        fileChannel.position(position);
        return position;
    }
}
