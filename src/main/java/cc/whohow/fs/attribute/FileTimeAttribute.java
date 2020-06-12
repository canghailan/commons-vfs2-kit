package cc.whohow.fs.attribute;

import java.nio.file.attribute.FileTime;

public class FileTimeAttribute extends AbstractFileTimeAttribute {
    protected final String name;
    protected final FileTime value;

    public FileTimeAttribute(String name, FileTime value) {
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
}
