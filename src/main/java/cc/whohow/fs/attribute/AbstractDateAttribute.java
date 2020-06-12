package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.util.FileTimes;

import java.nio.file.attribute.FileTime;
import java.util.Date;

public abstract class AbstractDateAttribute implements Attribute<Date> {
    @Override
    public Long getAsLong() {
        Date value = value();
        if (value == null) {
            return null;
        }
        return value.getTime();
    }

    @Override
    public Date getAsDate() {
        return value();
    }

    @Override
    public FileTime getAsFileTime() {
        Date value = value();
        if (value == null) {
            return null;
        }
        return FileTime.from(value.toInstant());
    }

    @Override
    public String getAsString() {
        return FileTimes.stringify(value());
    }

    @Override
    public String toString() {
        return name() + ": " + getAsString();
    }
}
