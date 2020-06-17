package cc.whohow.fs.attribute;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;

import java.util.*;

public class AttributesMap implements Attributes {
    protected final Map<String, Attribute<?>> attributes;

    public AttributesMap(Iterable<? extends Attribute<?>> attributes) {
        this.attributes = new LinkedHashMap<>();
        for (Attribute<?> attribute : attributes) {
            this.attributes.put(attribute.name(), attribute);
        }
    }

    public AttributesMap(Map<String, Attribute<?>> attributes) {
        Objects.requireNonNull(attributes);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && getClass() == o.getClass()) {
            AttributesMap that = (AttributesMap) o;
            return attributes.equals(that.attributes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
