package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.file.FileBasedPropertiesConfiguration;

import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * Properties配置文件
 */
public class PropertiesConfiguration extends CacheableConfiguration<Properties> {
    public PropertiesConfiguration(Configuration<ByteBuffer> source) {
        super(new FileBasedPropertiesConfiguration(source));
    }
}
