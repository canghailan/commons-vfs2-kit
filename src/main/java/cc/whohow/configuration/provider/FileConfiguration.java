package cc.whohow.configuration.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.BinaryType;

import java.nio.ByteBuffer;

public class FileConfiguration extends FileValue.Cache<ByteBuffer> {
    public FileConfiguration(FileObject fileObject) {
        super(fileObject, BinaryType.get());
    }
}
