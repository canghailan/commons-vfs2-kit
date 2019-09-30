package cc.whohow.vfs.diff;

import java.util.Map;

public class KeyValue<V> implements Map.Entry<String, V> {
    private final String key;
    private final V value;

    public KeyValue(String key, V value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return key + ": " + value;
    }
}
