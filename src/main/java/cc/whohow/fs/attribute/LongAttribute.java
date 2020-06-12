package cc.whohow.fs.attribute;

public class LongAttribute extends AbstractLongAttribute {
    protected final String name;
    protected final Long value;

    public LongAttribute(String name, Long value) {
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
}
