package cc.whohow.vfs.util;

import java.util.Iterator;
import java.util.function.Function;

public class MapIterable<T, R> implements Iterable<R> {
    private final Iterable<T> iterable;
    private final Function<T, R> function;

    public MapIterable(Iterable<T> iterable, Function<T, R> function) {
        this.iterable = iterable;
        this.function = function;
    }

    @Override
    public Iterator<R> iterator() {
        return new MapIterator<>(iterable.iterator(), function);
    }
}
