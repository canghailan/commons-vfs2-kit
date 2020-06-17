package cc.whohow.fs.attribute;

import java.util.Objects;

public class LongAttribute extends AbstractLongAttribute {
    protected final String name;
    protected final Long value;

    public LongAttribute(String name, Long value) {
        Objects.requireNonNull(name);
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof LongAttribute) {
            LongAttribute that = (LongAttribute) o;
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
