package cc.whohow.configuration.provider;

import cc.whohow.fs.util.ByteBufferReadableChannel;
import cc.whohow.fs.util.ByteBufferWritableChannel;
import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * Properties配置文件
 */
public class PropertiesConfiguration extends AbstractFileBasedConfiguration<Properties> {
    public PropertiesConfiguration(FileObject fileObject) {
        super(fileObject);
    }

    @Override
    protected ByteBuffer serialize(Properties value) throws IOException {
        try (ByteBufferWritableChannel channel = new ByteBufferWritableChannel()) {
            value.store(channel, null);
            return channel.getByteBuffer();
        }
    }

    @Override
    protected Properties deserialize(ByteBuffer bytes) throws IOException {
        try (ByteBufferReadableChannel channel = new ByteBufferReadableChannel(bytes)) {
            Properties properties = new Properties();
            properties.load(channel);
            return properties;
        }
    }
}
