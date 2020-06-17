package cc.whohow.fs.attribute;

import java.nio.file.attribute.FileTime;
import java.util.Objects;

public class FileTimeAttribute extends AbstractFileTimeAttribute {
    protected final String name;
    protected final FileTime value;

    public FileTimeAttribute(String name, FileTime value) {
        Objects.requireNonNull(name);
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public FileTime value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FileTimeAttribute) {
            FileTimeAttribute that = (FileTimeAttribute) o;
            return name.equals(that.name) &&
                    Objects.equals(value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
