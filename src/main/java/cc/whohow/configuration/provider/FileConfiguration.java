package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.serialize.BinarySerializer;
import cc.whohow.vfs.serialize.FileValue;

import java.nio.ByteBuffer;

public class FileConfiguration extends FileValue.Cache<ByteBuffer> implements Configuration<ByteBuffer> {
    public FileConfiguration(FileObjectX fileObject) {
        super(fileObject, BinarySerializer.get());
    }
}
