package cc.whohow.vfs.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Json配置文件
 */
public class JsonConfigurationFile implements ConfigurationFile {
    private static final ObjectMapper OBJECT_MAPPER = defaultObjectMapper();
    protected final JsonNode configuration;

    public JsonConfigurationFile(JsonNode configuration) {
        this.configuration = configuration;
    }

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public <T> T get(String name, Type type) {
        return OBJECT_MAPPER.convertValue(configuration.at(name), OBJECT_MAPPER.constructType(type));
    }

    @Override
    public Map<String, String> getJunctions() {
        return get("/vfs", Map.class);
    }

    @Override
    public <T> T getProviderConfigurations(Type type) {
        return get("/providers", type);
    }

    @Override
    public <T> T getProviderConfiguration(String name, Type type) {
        for (JsonNode provider : configuration.at("/providers")) {
            if (name.equals(provider.path("className").textValue())) {
                return OBJECT_MAPPER.convertValue(provider, OBJECT_MAPPER.constructType(type));
            }
        }
        return null;
    }

    @Override
    public <T> T getOperationProviderConfigurations(Type type) {
        return get("/operationProviders", type);
    }

    @Override
    public <T> T getOperationProviderConfiguration(String name, Type type) {
        for (JsonNode provider : configuration.at("/operationProviders")) {
            if (name.equals(provider.path("className").textValue())) {
                return OBJECT_MAPPER.convertValue(provider, OBJECT_MAPPER.constructType(type));
            }
        }
        return null;
    }
}
