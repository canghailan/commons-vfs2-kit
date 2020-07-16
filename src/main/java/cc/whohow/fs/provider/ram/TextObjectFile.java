package cc.whohow.fs.provider.ram;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.util.ByteBufferFileReadableChannel;
import cc.whohow.fs.util.ByteBufferFileWritableChannel;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

class TextObjectFile implements RamObjectFile {
    protected final URI uri;
    protected final Charset charset;
    protected final FileAttributes attributes;
    protected CharSequence content;

    TextObjectFile(URI uri, Charset charset, FileAttributes attributes, CharSequence content) {
        this.uri = uri;
        this.charset = charset;
        this.attributes = attributes;
        this.content = (content == null) ? "" : content;
    }

    @Override
    public RamObjectFile asBinaryObjectFile() {
        return new BinaryObjectFile(uri, attributes, read());
    }

    @Override
    public RamObjectFile asTextObjectFile(Charset charset) {
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
        return new ByteBufferFileWritableChannel() {
            @Override
            public void close() throws IOException {
                super.close();
                TextObjectFile.this.write(getByteBuffer());
            }
        };
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
        Objects.requireNonNull(content);
        write(charset.decode(content), charset);
    }

    @Override
    public String read(Charset charset) {
        return content.toString();
    }

    @Override
    public void write(CharSequence content, Charset charset) {
        Objects.requireNonNull(content);
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TextObjectFile) {
            TextObjectFile that = (TextObjectFile) o;
            return uri.equals(that.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
