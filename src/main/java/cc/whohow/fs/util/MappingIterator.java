package cc.whohow.fs.util;

import java.util.Iterator;
import java.util.function.Function;

public class MappingIterator<T, R> implements Iterator<R> {
    private final Iterator<T> iterator;
    private final Function<T, R> function;

    public MappingIterator(Iterator<T> iterator, Function<T, R> function) {
        this.iterator = iterator;
        this.function = function;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public R next() {
        return function.apply(iterator.next());
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
