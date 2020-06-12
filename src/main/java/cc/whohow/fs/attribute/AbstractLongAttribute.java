package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;

public abstract class AbstractLongAttribute implements Attribute<Long> {
    @Override
    public Long getAsLong() {
        return value();
    }

    @Override
    public FileTime getAsFileTime() {
        Long value = value();
        if (value == null) {
            return null;
        }
        return FileTime.from(Instant.ofEpochMilli(value));
    }

    @Override
    public Date getAsDate() {
        Long value = value();
        if (value == null) {
            return null;
        }
        return new Date(value);
    }

    @Override
    public String toString() {
        return name() + ": " + value();
    }
}
