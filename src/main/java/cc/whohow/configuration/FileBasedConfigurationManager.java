package cc.whohow.configuration;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.serialize.Serializer;

import java.util.List;

public interface FileBasedConfigurationManager {
    CloudFileObject get(String key);

    List<String> list(String key);

    <T> T get(String key, Serializer<T> serializer);
}
