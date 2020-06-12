package cc.whohow.fs.util;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilterIterable<E> implements Iterable<E> {
    private final Iterable<E> iterable;
    private final Predicate<E> filter;

    public FilterIterable(Iterable<E> iterable, Predicate<E> filter) {
        this.iterable = iterable;
        this.filter = filter;
    }

    @Override
    public Iterator<E> iterator() {
        return new FilterIterator<>(iterable.iterator(), filter);
    }
}
