package cc.whohow.fs.provider.http;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.util.FileTimes;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpMessage;

import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

public class HttpFileAttributes implements FileAttributes {
    private final HttpMessage httpMessage;

    public HttpFileAttributes(HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
    }

    @Override
    public FileTime lastModifiedTime() {
        return get(HttpHeaders.LAST_MODIFIED)
                .map(Attribute::getAsFileTime)
                .orElse(FileTimes.epoch());
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTimes.epoch();
    }

    @Override
    public FileTime creationTime() {
        return FileTimes.epoch();
    }

    @Override
    public long size() {
        return get(HttpHeaders.CONTENT_LENGTH)
                .map(Attribute::getAsLong)
                .orElse(0L);
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        Header header = httpMessage.getFirstHeader(name);
        if (header == null) {
            return Optional.empty();
        }
        return Optional.of(new HttpAttribute(header));
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return Arrays.stream(httpMessage.getAllHeaders())
                .<Attribute<?>>map(HttpAttribute::new)
                .iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof HttpFileAttributes) {
            HttpFileAttributes that = (HttpFileAttributes) o;
            return Arrays.equals(httpMessage.getAllHeaders(), that.httpMessage.getAllHeaders());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(httpMessage.getAllHeaders());
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
