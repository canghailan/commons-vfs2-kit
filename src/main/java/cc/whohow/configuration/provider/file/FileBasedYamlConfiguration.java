package cc.whohow.configuration.provider.file;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.AbstractMappingConfiguration;
import cc.whohow.fs.util.ByteBufferReadableChannel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * YAML配置文件
 */
public class FileBasedYamlConfiguration<T> extends AbstractMappingConfiguration<ByteBuffer, T> {
    protected static final YAMLMapper OBJECT_MAPPER = new YAMLMapper();

    static {
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected final JavaType type;
    protected final ObjectMapper objectMapper;

    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, Class<T> type) {
        this(source, type, OBJECT_MAPPER);
    }

    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, TypeReference<T> type) {
        this(source, type, OBJECT_MAPPER);
    }

    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, String type) {
        this(source, type, OBJECT_MAPPER);
    }

    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, JavaType type) {
        this(source, type, OBJECT_MAPPER);
    }

    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, Class<T> type, ObjectMapper objectMapper) {
        this(source, objectMapper.getTypeFactory().constructType(type), objectMapper);
    }


    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, TypeReference<T> type, ObjectMapper objectMapper) {
        this(source, objectMapper.getTypeFactory().constructType(type), objectMapper);
    }


    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, String type, ObjectMapper objectMapper) {
        this(source, objectMapper.getTypeFactory().constructFromCanonical(type), objectMapper);
    }

    public FileBasedYamlConfiguration(Configuration<ByteBuffer> source, JavaType type, ObjectMapper objectMapper) {
        super(source);
        this.type = type;
        this.objectMapper = objectMapper;
    }

    @Override
    protected ByteBuffer toSource(T value) {
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected T toTarget(ByteBuffer byteBuffer) {
        try {
            return objectMapper.readValue(new ByteBufferReadableChannel(byteBuffer), type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}