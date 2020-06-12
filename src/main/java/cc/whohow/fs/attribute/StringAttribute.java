package cc.whohow.fs.attribute;

public class StringAttribute extends AbstractStringAttribute {
    protected final String name;
    protected final String value;

    public StringAttribute(String name, String value) {
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
}
