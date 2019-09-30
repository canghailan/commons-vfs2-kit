package cc.whohow.vfs.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonType<T> implements DataType<T> {
    protected ObjectMapper objectMapper;
    protected JavaType type;

    public JsonType(ObjectMapper objectMapper, Class<T> type) {
        this(objectMapper, objectMapper.getTypeFactory().constructType(type));
    }

    public JsonType(ObjectMapper objectMapper, TypeReference<T> type) {
        this(objectMapper, objectMapper.getTypeFactory().constructType(type));
    }

    public JsonType(ObjectMapper objectMapper, String type) {
        this(objectMapper, objectMapper.getTypeFactory().constructFromCanonical(type));
    }

    public JsonType(ObjectMapper objectMapper, JavaType type) {
        this.objectMapper = objectMapper;
        this.type = type;
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
