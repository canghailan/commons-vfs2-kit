package cc.whohow.vfs.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesSerializer implements Serializer<Properties> {
    private static final PropertiesSerializer INSTANCE = new PropertiesSerializer();

    public static Serializer<Properties> get() {
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
