package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.serialize.YamlSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlConfiguration<T> extends JsonConfiguration<T> implements Configuration<T> {
    public YamlConfiguration(CloudFileObject fileObject, Class<T> type) {
        super(fileObject, new YamlSerializer<T>(type));
    }

    public YamlConfiguration(CloudFileObject fileObject, TypeReference<T> type) {
        super(fileObject, new YamlSerializer<T>(type));
    }

    public YamlConfiguration(CloudFileObject fileObject, String type) {
        super(fileObject, new YamlSerializer<T>(type));
    }

    public YamlConfiguration(CloudFileObject fileObject, JavaType type) {
        super(fileObject, new YamlSerializer<T>(type));
    }

    public YamlConfiguration(CloudFileObject fileObject, Class<T> type, ObjectMapper objectMapper) {
        super(fileObject, type, objectMapper);
    }

    public YamlConfiguration(CloudFileObject fileObject, TypeReference<T> type, ObjectMapper objectMapper) {
        super(fileObject, type, objectMapper);
    }

    public YamlConfiguration(CloudFileObject fileObject, String typeCanonicalName, ObjectMapper objectMapper) {
        super(fileObject, typeCanonicalName, objectMapper);
    }

    public YamlConfiguration(CloudFileObject fileObject, JavaType type, ObjectMapper objectMapper) {
        super(fileObject, type, objectMapper);
    }
}
