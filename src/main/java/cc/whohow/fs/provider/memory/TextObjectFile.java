package cc.whohow.fs.provider.memory;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.channel.ByteBufferFileReadableChannel;
import cc.whohow.fs.channel.ByteBufferFileWritableChannel;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class TextObjectFile implements MemoryObjectFile {
    private final URI uri;
    private final Charset charset;
    private final FileAttributes attributes;
    private CharSequence content;

    TextObjectFile(URI uri, Charset charset, FileAttributes attributes, CharSequence content) {
        this.uri = uri;
        this.charset = charset;
        this.attributes = attributes;
        this.content = content;
    }

    @Override
    public MemoryObjectFile asBinaryObjectFile() {
        return new BinaryObjectFile(uri, attributes, read());
    }

    @Override
    public MemoryObjectFile asTextObjectFile(Charset charset) {
        if (charset == this.charset || charset.equals(this.charset)) {
            return this;
        } else {
            return new TextObjectFile(uri, charset, attributes, content);
        }
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
        return charset.encode(read(charset));
    }

    @Override
    public void write(ByteBuffer content) {
        write(charset.decode(content), charset);
    }

    @Override
    public String read(Charset charset) {
        return content.toString();
    }

    @Override
    public void write(CharSequence content, Charset charset) {
        this.content = content;
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
