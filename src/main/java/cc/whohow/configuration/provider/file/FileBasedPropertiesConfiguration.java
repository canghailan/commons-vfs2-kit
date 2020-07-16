package cc.whohow.configuration.provider.file;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.AbstractMappingConfiguration;
import cc.whohow.fs.util.ByteBufferReadableChannel;
import cc.whohow.fs.util.ByteBufferWritableChannel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * Properties配置文件
 */
public class FileBasedPropertiesConfiguration extends AbstractMappingConfiguration<ByteBuffer, Properties> {
    public FileBasedPropertiesConfiguration(Configuration<ByteBuffer> source) {
        super(source);
    }

    @Override
    protected ByteBuffer toSource(Properties properties) {
        try (ByteBufferWritableChannel channel = new ByteBufferWritableChannel()) {
            properties.store(channel, null);
            return channel.getByteBuffer();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected Properties toTarget(ByteBuffer byteBuffer) {
        try (ByteBufferReadableChannel channel = new ByteBufferReadableChannel(byteBuffer)) {
            Properties properties = new Properties();
            properties.load(channel);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
