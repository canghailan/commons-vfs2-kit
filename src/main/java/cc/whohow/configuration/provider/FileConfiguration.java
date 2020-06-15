package cc.whohow.configuration.provider;

import org.apache.commons.vfs2.FileObject;

import java.nio.ByteBuffer;

public class FileConfiguration extends AbstractFileBasedConfiguration<ByteBuffer> {
    public FileConfiguration(FileObject fileObject) {
        super(fileObject);
    }

    @Override
    protected ByteBuffer serialize(ByteBuffer value) {
        return value.duplicate();
    }

    @Override
    protected ByteBuffer deserialize(ByteBuffer bytes) {
        return bytes;
    }
}
