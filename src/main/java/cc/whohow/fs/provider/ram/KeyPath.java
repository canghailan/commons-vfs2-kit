package cc.whohow.fs.provider.ram;

import cc.whohow.fs.provider.UriPath;
import cc.whohow.fs.util.Paths;

import java.net.URI;

public class KeyPath extends UriPath {
    private final String key;

    public KeyPath(URI uri, String key) {
        super(uri);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public KeyPath getParent() {
        String parent = Paths.getParent(key);
        if (parent == null) {
            return null;
        }
        return new KeyPath(Paths.getParent(uri), parent);
    }

    public boolean isAncestorKey(String key) {
        return key.startsWith(this.key);
    }
}
