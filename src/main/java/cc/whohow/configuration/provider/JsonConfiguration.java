package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.file.FileBasedJsonConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

/**
 * JSON配置文件
 */
public class JsonConfiguration<T> extends CacheableConfiguration<T> {
    public JsonConfiguration(Configuration<ByteBuffer> source, Class<T> type) {
        super(new FileBasedJsonConfiguration<T>(source, type));
    }

    public JsonConfiguration(Configuration<ByteBuffer> source, TypeReference<T> type) {
        super(new FileBasedJsonConfiguration<T>(source, type));
    }

    public JsonConfiguration(Configuration<ByteBuffer> source, String type) {
        super(new FileBasedJsonConfiguration<T>(source, type));
    }

    public JsonConfiguration(Configuration<ByteBuffer> source, JavaType type) {
        super(new FileBasedJsonConfiguration<T>(source, type));
    }

    public JsonConfiguration(Configuration<ByteBuffer> source, Class<T> type, ObjectMapper objectMapper) {
        super(new FileBasedJsonConfiguration<T>(source, type, objectMapper));
    }


    public JsonConfiguration(Configuration<ByteBuffer> source, TypeReference<T> type, ObjectMapper objectMapper) {
        super(new FileBasedJsonConfiguration<T>(source, type, objectMapper));
    }


    public JsonConfiguration(Configuration<ByteBuffer> source, String type, ObjectMapper objectMapper) {
        super(new FileBasedJsonConfiguration<T>(source, type, objectMapper));
    }

    public JsonConfiguration(Configuration<ByteBuffer> source, JavaType type, ObjectMapper objectMapper) {
        super(new FileBasedJsonConfiguration<T>(source, type, objectMapper));
    }
}
