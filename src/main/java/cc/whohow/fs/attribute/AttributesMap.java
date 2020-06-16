package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AttributesMap implements Attributes {
    protected final Map<String, Attribute<?>> attributes;

    public AttributesMap(Iterable<? extends Attribute<?>> attributes) {
        this.attributes = new LinkedHashMap<>();
        for (Attribute<?> attribute : attributes) {
            this.attributes.put(attribute.name(), attribute);
        }
    }

    public AttributesMap(Map<String, Attribute<?>> attributes) {
        this.attributes = attributes;
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
