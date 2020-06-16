package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileSystemAttributes;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class FileSystemAttributesMap extends AttributesMap implements FileSystemAttributes {
    public FileSystemAttributesMap(Iterable<? extends Attribute<?>> attributes) {
        super(attributes);
    }

    public FileSystemAttributesMap(Map<String, Attribute<?>> attributes) {
        super(attributes);
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
