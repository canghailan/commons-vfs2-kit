package cc.whohow.configuration.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.JsonType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConfiguration<T> extends FileValue.Cache<T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonConfiguration(FileObject fileObject, Class<T> type) {
        this(fileObject, type, OBJECT_MAPPER);
    }

    public JsonConfiguration(FileObject fileObject, TypeReference<T> type) {
        this(fileObject, type, OBJECT_MAPPER);
    }

    public JsonConfiguration(FileObject fileObject, String type) {
        this(fileObject, type, OBJECT_MAPPER);
    }

    public JsonConfiguration(FileObject fileObject, JavaType type) {
        this(fileObject, type, OBJECT_MAPPER);
    }

    public JsonConfiguration(FileObject fileObject, Class<T> type, ObjectMapper objectMapper) {
        super(fileObject, new JsonType<>(objectMapper, type));
    }

    public JsonConfiguration(FileObject fileObject, TypeReference<T> type, ObjectMapper objectMapper) {
        super(fileObject, new JsonType<>(objectMapper, type));
    }

    public JsonConfiguration(FileObject fileObject, String type, ObjectMapper objectMapper) {
        super(fileObject, new JsonType<>(objectMapper, type));
    }

    public JsonConfiguration(FileObject fileObject, JavaType type, ObjectMapper objectMapper) {
        super(fileObject, new JsonType<>(objectMapper, type));
    }
}
