package cc.whohow.fs.path;

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

    public boolean isAncestorKey(String key) {
        return key.startsWith(this.key);
    }
}
