package cc.whohow.vfs.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlSerializer<T> extends JsonSerializer<T> {
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();
    private static final YamlSerializer<JsonNode> INSTANCE = new YamlSerializer<>(JsonNode.class);

    public static YamlSerializer<JsonNode> get() {
        return INSTANCE;
    }

    public YamlSerializer(Class<T> type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(TypeReference<T> type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(String type) {
        super(YAML_MAPPER, type);
    }

    public YamlSerializer(JavaType type) {
        super(YAML_MAPPER, type);
    }
}
