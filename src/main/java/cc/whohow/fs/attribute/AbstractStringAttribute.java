package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.util.FileTimes;

import java.nio.file.attribute.FileTime;
import java.util.Date;

public abstract class AbstractStringAttribute implements Attribute<String> {
    @Override
    public Long getAsLong() {
        String value = value();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Override
    public FileTime getAsFileTime() {
        String value = value();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return FileTime.from(FileTimes.parse(value).toInstant());
    }

    @Override
    public Date getAsDate() {
        String value = value();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Date.from(FileTimes.parse(value).toInstant());
    }

    @Override
    public String getAsString() {
        return value();
    }

    @Override
    public String toString() {
        return name() + ": " + value();
    }
}
