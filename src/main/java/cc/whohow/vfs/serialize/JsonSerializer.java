package cc.whohow.vfs.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonSerializer<T> implements Serializer<T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonSerializer<JsonNode> INSTANCE = new JsonSerializer<>(JsonNode.class);
    protected ObjectMapper objectMapper;
    protected JavaType type;

    public JsonSerializer(Class<T> type) {
        this(OBJECT_MAPPER, type);
    }

    public JsonSerializer(TypeReference<T> type) {
        this(OBJECT_MAPPER, type);
    }

    public JsonSerializer(String type) {
        this(OBJECT_MAPPER, type);
    }

    public JsonSerializer(JavaType type) {
        this(OBJECT_MAPPER, type);
    }

    public JsonSerializer(ObjectMapper objectMapper, Class<T> type) {
        this(objectMapper, objectMapper.getTypeFactory().constructType(type));
    }

    public JsonSerializer(ObjectMapper objectMapper, TypeReference<T> type) {
        this(objectMapper, objectMapper.getTypeFactory().constructType(type));
    }

    public JsonSerializer(ObjectMapper objectMapper, String type) {
        this(objectMapper, objectMapper.getTypeFactory().constructFromCanonical(type));
    }

    public JsonSerializer(ObjectMapper objectMapper, JavaType type) {
        this.objectMapper = objectMapper;
        this.type = type;
    }

    public static JsonSerializer<JsonNode> get() {
        return INSTANCE;
    }

    @Override
    public T deserialize(InputStream stream) throws IOException {
        return objectMapper.readValue(stream, type);
    }

    @Override
    public void serialize(OutputStream stream, T value) throws IOException {
        objectMapper.writeValue(stream, value);
    }
}
