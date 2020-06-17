package cc.whohow.fs.provider.http;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.attribute.AbstractStringAttribute;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.client.utils.DateUtils;

import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Objects;

public class HttpAttribute extends AbstractStringAttribute {
    private final String name;
    private final Header header;

    public HttpAttribute(Header header) {
        Objects.requireNonNull(header);
        this.name = header.getName();
        this.header = header;
    }

    public HttpAttribute(HttpMessage httpMessage, String name) {
        Objects.requireNonNull(name);
        this.name = name;
        this.header = httpMessage.getFirstHeader(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return header == null ? null : header.getValue();
    }

    @Override
    public Date getAsDate() {
        String value = value();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return DateUtils.parseDate(value);
    }

    @Override
    public FileTime getAsFileTime() {
        Date date = getAsDate();
        if (date == null) {
            return null;
        }
        return FileTime.from(date.toInstant());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof HttpAttribute) {
            HttpAttribute that = (HttpAttribute) o;
            return name.equals(that.name) &&
                    Objects.equals(header, that.header);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, header);
    }

    @Override
    public String toString() {
        return Attribute.toString(this);
    }
}
