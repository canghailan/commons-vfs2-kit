package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.util.FileTimes;

import java.nio.file.attribute.FileTime;
import java.util.Map;

public class FileAttributesMap extends AttributesMap implements FileAttributes {
    public FileAttributesMap(Iterable<? extends Attribute<?>> attributes) {
        super(attributes);
    }

    public FileAttributesMap(Map<String, Attribute<?>> attributes) {
        super(attributes);
    }

    @Override
    public FileTime lastModifiedTime() {
        return getAsFileTime(LAST_MODIFIED_TIME).orElse(FileTimes.epoch());
    }

    @Override
    public FileTime lastAccessTime() {
        return getAsFileTime(LAST_ACCESS_TIME).orElse(FileTimes.epoch());
    }

    @Override
    public FileTime creationTime() {
        return getAsFileTime(CREATION_TIME).orElse(FileTimes.epoch());
    }

    @Override
    public long size() {
        return getAsLong(SIZE).orElse(0L);
    }
}
