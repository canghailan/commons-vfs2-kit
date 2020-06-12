package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.util.FileTimes;

import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class FileAttributesMap implements FileAttributes {
    protected final Map<String, Attribute<?>> attributes;

    public FileAttributesMap(Iterable<? extends Attribute<?>> attributes) {
        this.attributes = new LinkedHashMap<>();
        for (Attribute<?> attribute : attributes) {
            this.attributes.put(attribute.name(), attribute);
        }
    }

    public FileAttributesMap(Map<String, Attribute<?>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public FileTime lastModifiedTime() {
        return getAsFileTime("lastModifiedTime").orElse(FileTimes.epoch());
    }

    @Override
    public FileTime lastAccessTime() {
        return getAsFileTime("lastAccessTime").orElse(FileTimes.epoch());
    }

    @Override
    public FileTime creationTime() {
        return getAsFileTime("creationTime").orElse(FileTimes.epoch());
    }

    @Override
    public long size() {
        return getAsLong("size").orElse(0L);
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return attributes.values().iterator();
    }
}
