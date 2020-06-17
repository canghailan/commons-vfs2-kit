package cc.whohow.fs.attribute;

import java.util.Objects;

public class StringAttribute extends AbstractStringAttribute {
    protected final String name;
    protected final String value;

    public StringAttribute(String name, String value) {
        Objects.requireNonNull(name);
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StringAttribute) {
            StringAttribute that = (StringAttribute) o;
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
