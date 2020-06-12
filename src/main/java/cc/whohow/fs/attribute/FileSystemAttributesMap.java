package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileSystemAttributes;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class FileSystemAttributesMap implements FileSystemAttributes {
    protected final Map<String, Attribute<?>> attributes;

    public FileSystemAttributesMap(Iterable<? extends Attribute<?>> attributes) {
        this.attributes = new LinkedHashMap<>();
        for (Attribute<?> attribute : attributes) {
            this.attributes.put(attribute.name(), attribute);
        }
    }

    public FileSystemAttributesMap(Map<String, Attribute<?>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String provider() {
        return getAsString("provider").orElse("");
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
