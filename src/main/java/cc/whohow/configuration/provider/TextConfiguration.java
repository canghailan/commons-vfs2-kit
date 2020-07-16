package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.file.FileBasedTextConfiguration;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 文本配置文件
 */
public class TextConfiguration extends CacheableConfiguration<String> {
    public TextConfiguration(Configuration<ByteBuffer> source) {
        super(new FileBasedTextConfiguration(source));
    }

    public TextConfiguration(Configuration<ByteBuffer> source, Charset charset) {
        super(new FileBasedTextConfiguration(source, charset));
    }
}
