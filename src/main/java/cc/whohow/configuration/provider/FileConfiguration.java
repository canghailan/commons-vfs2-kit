package cc.whohow.configuration.provider;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.BinaryType;

import java.nio.ByteBuffer;

public class FileConfiguration extends FileValue.Cache<ByteBuffer> {
    public FileConfiguration(CloudFileObject fileObject) {
        super(fileObject, BinaryType.get());
    }
}
