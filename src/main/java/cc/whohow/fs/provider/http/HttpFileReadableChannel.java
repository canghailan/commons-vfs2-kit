package cc.whohow.fs.provider.http;

import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.io.FileReadableStream;
import cc.whohow.fs.io.IO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class HttpFileReadableChannel extends FileReadableStream {
    private final CloseableHttpResponse httpResponse;
    private final HttpEntity httpEntity;

    public HttpFileReadableChannel(CloseableHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.httpResponse = httpResponse;
        this.httpEntity = httpResponse.getEntity();
    }

    @Override
    public ByteBuffer readAllBytes() throws IOException {
        if (httpEntity.getContentLength() > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("file too large");
        }
        if (httpEntity.getContentLength() < 0) {
            return IO.read(stream);
        } else {
            return IO.read(stream, (int) httpEntity.getContentLength());
        }
    }

    @Override
    public long transferTo(OutputStream stream) throws IOException {
        httpEntity.writeTo(stream);
        return httpEntity.getContentLength();
    }

    @Override
    public long transferTo(WritableByteChannel channel) throws IOException {
        return transferTo(Channels.newOutputStream(channel));
    }

    @Override
    public long transferTo(FileWritableChannel channel) throws IOException {
        return transferTo(channel.stream());
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            httpResponse.close();
        }
    }
}
