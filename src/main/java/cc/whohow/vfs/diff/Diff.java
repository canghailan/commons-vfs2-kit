package cc.whohow.vfs.diff;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * diff工具
 */
public class Diff<K, V> {
    private boolean all = false;

    public Diff() {
    }

    public Map<K, DiffStatus> diff(Map<K, V> oldValues, Map<K, V> newValues) {
        Map<K, DiffStatus> diffs = new LinkedHashMap<>();
        for (Map.Entry<K, V> newValue : newValues.entrySet()) {
            K key = newValue.getKey();
            V oldValue = oldValues.remove(key);
            if (oldValue == null) {
                diffs.put(key, DiffStatus.ADDED);
            } else if (Objects.equals(oldValue, newValue.getValue())) {
                if (all) {
                    diffs.put(key, DiffStatus.NOT_MODIFIED);
                }
            } else {
                diffs.put(key, DiffStatus.MODIFIED);
            }
        }
        for (K key : oldValues.keySet()) {
            diffs.put(key, DiffStatus.DELETED);
        }
        return diffs;
    }
}
