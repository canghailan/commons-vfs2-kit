package cc.whohow.vfs.diff;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MapDiffs<T, K, V> implements Iterator<Diff<K>> {
    private Function<T, K> key;
    private Function<T, V> value;
    private BiPredicate<V, V> equals;
    private Map<K, V> oldIndex;
    private Iterator<T> newList;
    private Iterator<K> delete;

    public MapDiffs(Function<T, K> key, Function<T, V> value, Iterator<T> newList, Iterator<T> oldList) {
        this(key, value, new LinkedHashMap<>(), newList, oldList);
    }

    public MapDiffs(Function<T, K> key, Function<T, V> value, Map<K, V> map, Iterator<T> newList, Iterator<T> oldList) {
        this(key, value, Objects::equals, map, newList, oldList);
    }

    public MapDiffs(Function<T, K> key, Function<T, V> value, BiPredicate<V, V> equals, Map<K, V> map, Iterator<T> newList, Iterator<T> oldList) {
        this.key = key;
        this.value = value;
        this.equals = equals;
        this.oldIndex = map;
        this.newList = newList;
        while (oldList.hasNext()) {
            T next = oldList.next();
            map.put(key.apply(next), value.apply(next));
        }
    }

    @Override
    public boolean hasNext() {
        if (delete != null) {
            return delete.hasNext();
        }
        if (newList.hasNext()) {
            return true;
        } else {
            delete = oldIndex.keySet().iterator();
            return delete.hasNext();
        }
    }

    @Override
    public Diff<K> next() {
        if (delete != null) {
            return Diff.delete(delete.next());
        }

        T next = newList.next();
        K newKey = key.apply(next);
        V newValue = value.apply(next);
        V oldValue = oldIndex.remove(newKey);
        if (oldValue == null) {
            return Diff.add(newKey);
        }
        if (equals.test(oldValue, newValue)) {
            return Diff.eq(newKey);
        } else {
            return Diff.update(newKey);
        }
    }

    public Stream<Diff<K>> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false);
    }
}
