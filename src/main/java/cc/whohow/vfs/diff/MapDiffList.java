package cc.whohow.vfs.diff;

import cc.whohow.vfs.util.CloseableIterable;
import cc.whohow.vfs.util.CloseableList;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MapDiffList<T> implements CloseableIterable<Diff> {
    protected BiPredicate<T, T> equals = Objects::equals;
    protected Iterable<Map.Entry<String, T>> source;
    protected Iterable<Map.Entry<String, T>> target;
    protected Runnable close;

    public MapDiffList(Iterable<Map.Entry<String, T>> source, Iterable<Map.Entry<String, T>> target) {
        this.source = source;
        this.target = target;
    }

    public MapDiffList(CloseableIterable<Map.Entry<String, T>> source, CloseableIterable<Map.Entry<String, T>> target) {
        this.source = source;
        this.target = target;
        this.close = new CloseableList(source, target);
    }

    public Stream<Diff> stream() {
        return StreamSupport.stream(spliterator(), false).onClose(this::close);
    }

    @Override
    public void close() {
        if (close != null) {
            close.run();
        }
    }

    @Override
    public Iterator<Diff> iterator() {
        return new MapDiffIterator<>(equals, source, target);
    }

    private static class MapDiffIterator<T> implements Iterator<Diff> {
        private BiPredicate<T, T> equals;
        private Map<String, T> source;
        private Iterator<Map.Entry<String, T>> target;
        private Iterator<String> delete;

        public MapDiffIterator(BiPredicate<T, T> equals, Iterable<Map.Entry<String, T>> source, Iterable<Map.Entry<String, T>> target) {
            this.equals = equals;
            this.source = new LinkedHashMap<>();
            for (Map.Entry<String, T> e : source) {
                this.source.put(e.getKey(), e.getValue());
            }
            this.target = target.iterator();
        }

        @Override
        public boolean hasNext() {
            if (delete != null) {
                return delete.hasNext();
            }
            if (target.hasNext()) {
                return true;
            } else {
                delete = source.keySet().iterator();
                return delete.hasNext();
            }
        }

        @Override
        public Diff next() {
            if (delete != null) {
                return Diff.delete(delete.next());
            }

            Map.Entry<String, T> keyValue = target.next();
            T value = source.remove(keyValue.getKey());
            if (value == null) {
                return Diff.add(keyValue.getKey());
            }
            if (equals.test(value, keyValue.getValue())) {
                return Diff.no(keyValue.getKey());
            } else {
                return Diff.update(keyValue.getKey());
            }
        }
    }
}
