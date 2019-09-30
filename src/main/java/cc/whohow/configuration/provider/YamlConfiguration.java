package cc.whohow.configuration.provider;

import cc.whohow.vfs.FileObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlConfiguration<T> extends JsonConfiguration<T> {
    private static final ObjectMapper DEFAULT_YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    public YamlConfiguration(FileObject fileObject, Class<T> type) {
        this(fileObject, type, DEFAULT_YAML_OBJECT_MAPPER);
    }

    public YamlConfiguration(FileObject fileObject, TypeReference<T> type) {
        this(fileObject, type, DEFAULT_YAML_OBJECT_MAPPER);
    }

    public YamlConfiguration(FileObject fileObject, String typeCanonicalName) {
        this(fileObject, typeCanonicalName, DEFAULT_YAML_OBJECT_MAPPER);
    }

    public YamlConfiguration(FileObject fileObject, JavaType type) {
        this(fileObject, type, DEFAULT_YAML_OBJECT_MAPPER);
    }

    public YamlConfiguration(FileObject fileObject, Class<T> type, ObjectMapper objectMapper) {
        super(fileObject, type, objectMapper);
    }

    public YamlConfiguration(FileObject fileObject, TypeReference<T> type, ObjectMapper objectMapper) {
        super(fileObject, type, objectMapper);
    }

    public YamlConfiguration(FileObject fileObject, String typeCanonicalName, ObjectMapper objectMapper) {
        super(fileObject, typeCanonicalName, objectMapper);
    }

    public YamlConfiguration(FileObject fileObject, JavaType type, ObjectMapper objectMapper) {
        super(fileObject, type, objectMapper);
    }
}
