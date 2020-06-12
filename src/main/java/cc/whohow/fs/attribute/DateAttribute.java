package cc.whohow.fs.attribute;

import java.util.Date;

public class DateAttribute extends AbstractDateAttribute {
    protected final String name;
    protected final Date value;

    public DateAttribute(String name, Date value) {
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
}
