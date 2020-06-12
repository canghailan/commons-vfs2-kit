package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.util.FileTimes;

import java.nio.file.attribute.FileTime;

public abstract class AbstractFileTimeAttribute implements Attribute<FileTime> {
    @Override
    public Long getAsLong() {
        FileTime value = value();
        if (value == null) {
            return null;
        }
        return value.toMillis();
    }

    @Override
    public FileTime getAsFileTime() {
        return value();
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
