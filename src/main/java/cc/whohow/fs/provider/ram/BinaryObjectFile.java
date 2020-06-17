package cc.whohow.fs.provider.ram;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.util.ByteBufferFileReadableChannel;
import cc.whohow.fs.util.ByteBufferFileWritableChannel;
import cc.whohow.fs.util.ByteBuffers;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

class BinaryObjectFile implements RamObjectFile {
    protected final URI uri;
    protected final FileAttributes attributes;
    protected ByteBuffer content;

    BinaryObjectFile(URI uri, FileAttributes attributes, ByteBuffer content) {
        this.uri = uri;
        this.attributes = attributes;
        this.content = (content == null) ? ByteBuffers.empty() : content;
    }

    @Override
    public RamObjectFile asBinaryObjectFile() {
        return this;
    }

    @Override
    public RamObjectFile asTextObjectFile(Charset charset) {
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
        Objects.requireNonNull(content);
        this.content = content;
    }

    @Override
    public String read(Charset charset) {
        return charset.decode(read()).toString();
    }

    @Override
    public void write(CharSequence content, Charset charset) {
        Objects.requireNonNull(content);
        this.content = charset.encode(CharBuffer.wrap(content));
    }
}
