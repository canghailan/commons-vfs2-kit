package cc.whohow.configuration.provider.file;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.AbstractMappingConfiguration;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文本配置文件
 */
public class FileBasedTextConfiguration extends AbstractMappingConfiguration<ByteBuffer, String> {
    protected final Charset charset;

    public FileBasedTextConfiguration(Configuration<ByteBuffer> source) {
        this(source, StandardCharsets.UTF_8);
    }

    public FileBasedTextConfiguration(Configuration<ByteBuffer> source, Charset charset) {
        super(source);
        this.charset = charset;
    }

    @Override
    protected ByteBuffer toSource(String text) {
        return charset.encode(text);
    }

    @Override
    protected String toTarget(ByteBuffer byteBuffer) {
        return charset.decode(byteBuffer).toString();
    }
}
