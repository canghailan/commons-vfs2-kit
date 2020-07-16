package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.file.FileBasedYamlConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

/**
 * YAML配置文件
 */
public class YamlConfiguration<T> extends CacheableConfiguration<T> {
    public YamlConfiguration(Configuration<ByteBuffer> source, Class<T> type) {
        super(new FileBasedYamlConfiguration<>(source, type));
    }

    public YamlConfiguration(Configuration<ByteBuffer> source, TypeReference<T> type) {
        super(new FileBasedYamlConfiguration<>(source, type));
    }

    public YamlConfiguration(Configuration<ByteBuffer> source, String type) {
        super(new FileBasedYamlConfiguration<>(source, type));
    }

    public YamlConfiguration(Configuration<ByteBuffer> source, JavaType type) {
        super(new FileBasedYamlConfiguration<>(source, type));
    }

    public YamlConfiguration(Configuration<ByteBuffer> source, Class<T> type, ObjectMapper objectMapper) {
        super(new FileBasedYamlConfiguration<>(source, type, objectMapper));
    }


    public YamlConfiguration(Configuration<ByteBuffer> source, TypeReference<T> type, ObjectMapper objectMapper) {
        super(new FileBasedYamlConfiguration<>(source, type, objectMapper));
    }


    public YamlConfiguration(Configuration<ByteBuffer> source, String type, ObjectMapper objectMapper) {
        super(new FileBasedYamlConfiguration<>(source, type, objectMapper));
    }

    public YamlConfiguration(Configuration<ByteBuffer> source, JavaType type, ObjectMapper objectMapper) {
        super(new FileBasedYamlConfiguration<>(source, type, objectMapper));
    }
}