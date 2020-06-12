package cc.whohow.fs.configuration;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class JsonConfigurationParser {
    protected ConfigurationBuilder configurationBuilder;
    protected Deque<String> path = new LinkedList<>();

    public JsonConfigurationParser(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public ConfigurationBuilder parse(JsonNode configuration) {
        Iterator<Map.Entry<String, JsonNode>> iterator = configuration.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> i = iterator.next();
            if ("vfs".equals(i.getKey())) {
                parseVfs(i.getValue());
            } else {
                parse(i.getKey(), i.getValue());
            }
        }
        return configurationBuilder;
    }

    protected void parseVfs(JsonNode vfs) {
        Iterator<Map.Entry<String, JsonNode>> iterator = vfs.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> i = iterator.next();
            configurationBuilder.configureVfs(i.getKey(), i.getValue().textValue());
        }
    }

    protected void parse(String key, JsonNode node) {
        path.addLast(key);

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> i = iterator.next();
                parse(i.getKey(), i.getValue());
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                parse(Integer.toString(i), node.get(i));
            }
        } else {
            StringJoiner name = new StringJoiner("/");
            path.forEach(name::add);
            configurationBuilder.configure(name.toString(), node.asText(null));
        }

        path.removeLast();
    }
}
