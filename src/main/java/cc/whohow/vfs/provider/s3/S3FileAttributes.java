package cc.whohow.vfs.provider.s3;

import cc.whohow.vfs.util.ConcatIterator;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class S3FileAttributes implements Map<String, Object> {
    protected final Map rawObjectMetadata;
    protected final Map userObjectMetadata;
    protected final Predicate isRawKey;

    public S3FileAttributes(Map<String, ?> rawObjectMetadata, Map<String, ?> userObjectMetadata) {
        this.rawObjectMetadata = (rawObjectMetadata == null) ? Collections.emptyMap() : rawObjectMetadata;
        this.userObjectMetadata = (userObjectMetadata == null) ? Collections.emptyMap() : userObjectMetadata;
        this.isRawKey = this.rawObjectMetadata::containsKey;
    }

    public S3FileAttributes(Map<String, ?> rawObjectMetadata, Map<String, ?> userObjectMetadata, Predicate<?> isRawKey) {
        this.rawObjectMetadata = (rawObjectMetadata == null) ? Collections.emptyMap() : rawObjectMetadata;
        this.userObjectMetadata = (userObjectMetadata == null) ? Collections.emptyMap() : userObjectMetadata;
        this.isRawKey = isRawKey;
    }

    @Override
    public int size() {
        return rawObjectMetadata.size() + userObjectMetadata.size();
    }

    @Override
    public boolean isEmpty() {
        return rawObjectMetadata.isEmpty() && userObjectMetadata.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return rawObjectMetadata.containsKey(key) || userObjectMetadata.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return rawObjectMetadata.containsValue(value) || userObjectMetadata.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return isRawKey.test(key) ? rawObjectMetadata.get(key) : userObjectMetadata.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.put(key, value);
        } else {
            return userObjectMetadata.put(key, value);
        }
    }

    @Override
    public Object remove(Object key) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.remove(key);
        } else {
            return userObjectMetadata.remove(key);
        }
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Map.Entry<? extends String, ?> e : m.entrySet()) {
            if (isRawKey.test(e.getKey())) {
                rawObjectMetadata.put(e.getKey(), e.getValue());
            } else {
                userObjectMetadata.put(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public void clear() {
        rawObjectMetadata.clear();
        userObjectMetadata.clear();
    }

    @Override
    public Set<String> keySet() {
        return new AbstractSet<String>() {
            public int size() {
                return S3FileAttributes.this.size();
            }

            public void clear() {
                S3FileAttributes.this.clear();
            }

            public Iterator<String> iterator() {
                return new ConcatIterator(Arrays.asList(rawObjectMetadata.keySet().iterator(), userObjectMetadata.keySet().iterator()).iterator());
            }

            public boolean contains(Object o) {
                return containsKey(o);
            }

            public boolean remove(Object key) {
                return isRawKey.test(key) ? rawObjectMetadata.keySet().remove(key) : userObjectMetadata.keySet().remove(key);
            }
        };
    }

    @Override
    public Collection<Object> values() {
        return new AbstractCollection<Object>() {
            public int size() {
                return S3FileAttributes.this.size();
            }

            public void clear() {
                S3FileAttributes.this.clear();
            }

            public Iterator<Object> iterator() {
                return new ConcatIterator(Arrays.asList(rawObjectMetadata.values().iterator(), userObjectMetadata.values().iterator()).iterator());
            }

            public boolean contains(Object o) {
                return containsValue(o);
            }
        };
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new AbstractSet<Entry<String, Object>>() {
            public int size() {
                return S3FileAttributes.this.size();
            }

            public void clear() {
                S3FileAttributes.this.clear();
            }

            public Iterator<Map.Entry<String, Object>> iterator() {
                return new ConcatIterator(Arrays.asList(rawObjectMetadata.entrySet().iterator(), userObjectMetadata.entrySet().iterator()).iterator());
            }

            public final boolean contains(Object o) {
                if (o instanceof Map.Entry) {
                    Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                    if (isRawKey.test(e.getKey())) {
                        return rawObjectMetadata.entrySet().contains(o);
                    } else {
                        return userObjectMetadata.entrySet().contains(o);
                    }
                }
                return false;
            }

            public final boolean remove(Object o) {
                if (o instanceof Map.Entry) {
                    Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                    if (isRawKey.test(e.getKey())) {
                        return rawObjectMetadata.entrySet().remove(o);
                    } else {
                        return userObjectMetadata.entrySet().remove(o);
                    }
                }
                return false;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof S3FileAttributes) {
            S3FileAttributes that = (S3FileAttributes) o;
            return this.rawObjectMetadata.equals(that.rawObjectMetadata) &&
                    this.userObjectMetadata.equals(that.userObjectMetadata);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return rawObjectMetadata.hashCode() * 31 + userObjectMetadata.hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.getOrDefault(key, defaultValue);
        } else {
            return userObjectMetadata.getOrDefault(key, defaultValue);
        }
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        rawObjectMetadata.forEach(action);
        userObjectMetadata.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        rawObjectMetadata.replaceAll(function);
        userObjectMetadata.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.putIfAbsent(key, value);
        } else {
            return userObjectMetadata.putIfAbsent(key, value);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.remove(key, value);
        } else {
            return userObjectMetadata.remove(key, value);
        }
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.replace(key, oldValue, newValue);
        } else {
            return userObjectMetadata.replace(key, oldValue, newValue);
        }
    }

    @Override
    public Object replace(String key, Object value) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.replace(key, value);
        } else {
            return userObjectMetadata.replace(key, value);
        }
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.computeIfAbsent(key, mappingFunction);
        } else {
            return userObjectMetadata.computeIfAbsent(key, mappingFunction);
        }
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.computeIfPresent(key, remappingFunction);
        } else {
            return userObjectMetadata.computeIfPresent(key, remappingFunction);
        }
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.compute(key, remappingFunction);
        } else {
            return userObjectMetadata.compute(key, remappingFunction);
        }
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        if (isRawKey.test(key)) {
            return rawObjectMetadata.merge(key, value, remappingFunction);
        } else {
            return userObjectMetadata.merge(key, value, remappingFunction);
        }
    }
}
