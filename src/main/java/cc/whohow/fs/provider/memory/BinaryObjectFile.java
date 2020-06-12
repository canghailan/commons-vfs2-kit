package cc.whohow.fs.provider.memory;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.channel.ByteBufferFileReadableChannel;
import cc.whohow.fs.channel.ByteBufferFileWritableChannel;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

class BinaryObjectFile implements MemoryObjectFile {
    private final URI uri;
    private FileAttributes attributes;
    private ByteBuffer content;

    BinaryObjectFile(URI uri, FileAttributes attributes, ByteBuffer content) {
        this.uri = uri;
        this.attributes = attributes;
        this.content = content;
    }

    @Override
    public MemoryObjectFile asBinaryObjectFile() {
        return this;
    }

    @Override
    public MemoryObjectFile asTextObjectFile(Charset charset) {
        return new TextObjectFile(uri, charset, attributes, read(charset));
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public FileAttributes readAttributes() {
        return attributes;
    }

    @Override
    public FileReadableChannel newReadableChannel() {
        return new ByteBufferFileReadableChannel(read());
    }

    @Override
    public FileWritableChannel newWritableChannel() {
        return new ByteBufferFileWritableChannel().onClose(this::write);
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuffer read() {
        return content.duplicate();
    }

    @Override
    public void write(ByteBuffer content) {
        this.content = content;
    }

    @Override
    public String read(Charset charset) {
        return charset.decode(read()).toString();
    }

    @Override
    public void write(CharSequence content, Charset charset) {
        this.content = charset.encode(CharBuffer.wrap(content));
    }
}
