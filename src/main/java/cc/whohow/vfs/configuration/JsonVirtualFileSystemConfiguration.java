package cc.whohow.vfs.configuration;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.VirtualFileSystemManager;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.vfs2.FileSystemException;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public class JsonVirtualFileSystemConfiguration {
    protected JsonNode json;
    protected ArrayDeque<String> path;
    protected VirtualFileSystem virtualFileSystem;

    public JsonVirtualFileSystemConfiguration(JsonNode json) {
        this.json = json;
    }

    public VirtualFileSystem build() {
        try {
            path = new ArrayDeque<>();
            virtualFileSystem = new VirtualFileSystemManager();

            Iterator<Map.Entry<String, JsonNode>> iterator = json.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> i = iterator.next();
                if ("vfs".equals(i.getKey())) {
                    continue;
                }
                visit(i.getKey(), i.getValue());
            }

            virtualFileSystem.init();

            Iterator<Map.Entry<String, JsonNode>> vfs = json.path("vfs").fields();
            while (vfs.hasNext()) {
                Map.Entry<String, JsonNode> i = vfs.next();
                CloudFileObject fileObject = virtualFileSystem.resolveFile(i.getValue().textValue());
                virtualFileSystem.addJunction(i.getKey(), fileObject);
                for (String uri : fileObject.getURIs()) {
                    virtualFileSystem.addJunction(uri, fileObject);
                }
            }

            return virtualFileSystem;
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    public void visit(String key, JsonNode node) throws FileSystemException {
        path.addLast(key);

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> i = iterator.next();
                visit(i.getKey(), i.getValue());
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                visit(Integer.toString(i), node.get(i));
            }
        } else {
            StringJoiner name = new StringJoiner("/", "/", "");
            path.forEach(name::add);
            virtualFileSystem.setAttribute(name.toString(), node.asText(null));
        }

        path.removeLast();
    }
}
