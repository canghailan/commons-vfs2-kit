package cc.whohow.vfs.type;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesType implements DataType<Properties> {
    private static final PropertiesType INSTANCE = new PropertiesType();

    public static DataType<Properties> get() {
        return INSTANCE;
    }

    @Override
    public Properties deserialize(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        return properties;
    }

    @Override
    public void serialize(OutputStream stream, Properties value) throws IOException {
        value.store(stream, null);
    }
}
