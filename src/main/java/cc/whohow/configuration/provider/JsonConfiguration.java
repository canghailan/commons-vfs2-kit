package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.serialize.JsonSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConfiguration<T> extends FileValue.Cache<T> implements Configuration<T> {
    public JsonConfiguration(CloudFileObject fileObject, Class<T> type) {
        super(fileObject, new JsonSerializer<>(type));
    }

    public JsonConfiguration(CloudFileObject fileObject, TypeReference<T> type) {
        super(fileObject, new JsonSerializer<>(type));
    }

    public JsonConfiguration(CloudFileObject fileObject, String type) {
        super(fileObject, new JsonSerializer<>(type));
    }

    public JsonConfiguration(CloudFileObject fileObject, JavaType type) {
        super(fileObject, new JsonSerializer<>(type));
    }

    public JsonConfiguration(CloudFileObject fileObject, Class<T> type, ObjectMapper objectMapper) {
        super(fileObject, new JsonSerializer<>(objectMapper, type));
    }

    public JsonConfiguration(CloudFileObject fileObject, TypeReference<T> type, ObjectMapper objectMapper) {
        super(fileObject, new JsonSerializer<>(objectMapper, type));
    }

    public JsonConfiguration(CloudFileObject fileObject, String type, ObjectMapper objectMapper) {
        super(fileObject, new JsonSerializer<>(objectMapper, type));
    }

    public JsonConfiguration(CloudFileObject fileObject, JavaType type, ObjectMapper objectMapper) {
        super(fileObject, new JsonSerializer<>(objectMapper, type));
    }
}
