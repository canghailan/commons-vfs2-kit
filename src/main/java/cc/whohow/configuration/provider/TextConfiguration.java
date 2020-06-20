package cc.whohow.configuration.provider;

import org.apache.commons.vfs2.FileObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文本配置文件
 */
public class TextConfiguration extends AbstractFileBasedConfiguration<String> {
    protected final Charset charset;

    public TextConfiguration(FileObject fileObject) {
        this(fileObject, StandardCharsets.UTF_8);
    }

    public TextConfiguration(FileObject fileObject, Charset charset) {
        super(fileObject);
        this.charset = charset;
    }

    @Override
    protected ByteBuffer serialize(String value) throws IOException {
        return charset.encode(value);
    }

    @Override
    protected String deserialize(ByteBuffer bytes) throws IOException {
        return charset.decode(bytes).toString();
    }
}
