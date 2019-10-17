package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.serialize.BinarySerializer;

import java.nio.ByteBuffer;

public class FileConfiguration extends FileValue.Cache<ByteBuffer> implements Configuration<ByteBuffer> {
    public FileConfiguration(CloudFileObject fileObject) {
        super(fileObject, BinarySerializer.get());
    }
}
