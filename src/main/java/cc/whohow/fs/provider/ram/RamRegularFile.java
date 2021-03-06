package cc.whohow.fs.provider.ram;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class RamRegularFile extends RamFile {
    protected RamObjectFile content;

    public RamRegularFile(RamFileSystem fileSystem, KeyPath path, RamObjectFile content) {
        super(fileSystem, path);
        this.content = content;
    }

    @Override
    public FileAttributes readAttributes() {
        return content.readAttributes();
    }

    @Override
    public FileReadableChannel newReadableChannel() {
        return content.newReadableChannel();
    }

    @Override
    public synchronized FileWritableChannel newWritableChannel() {
        fileSystem.save(this);
        content = content.asBinaryObjectFile();
        return content.newWritableChannel();
    }

    @Override
    public ByteBuffer read() {
        return content.read();
    }

    @Override
    public void write(ByteBuffer content) {
        fileSystem.save(this);
        this.content.write(content);
    }

    @Override
    public synchronized String read(Charset charset) {
        this.content = this.content.asTextObjectFile(charset);
        return this.content.read(charset);
    }

    @Override
    public synchronized void write(CharSequence content, Charset charset) {
        fileSystem.save(this);
        this.content = this.content.asTextObjectFile(charset);
        this.content.write(content, charset);
    }
}
