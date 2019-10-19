package cc.whohow.configuration;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.serialize.Serializer;

import java.util.List;

public interface FileBasedConfigurationManager {
    FileObjectX get(String key);

    List<String> list(String key);

    <T> T get(String key, Serializer<T> serializer);
}
