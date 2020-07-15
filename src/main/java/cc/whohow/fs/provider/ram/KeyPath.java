package cc.whohow.fs.provider.ram;

import cc.whohow.fs.provider.UriPath;

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
