package cc.whohow.fs.attribute;

import java.util.Date;
import java.util.Objects;

public class DateAttribute extends AbstractDateAttribute {
    protected final String name;
    protected final Date value;

    public DateAttribute(String name, Date value) {
        Objects.requireNonNull(name);
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Date value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DateAttribute) {
            DateAttribute that = (DateAttribute) o;
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
