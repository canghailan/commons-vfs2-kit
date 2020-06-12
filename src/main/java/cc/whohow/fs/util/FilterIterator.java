package cc.whohow.fs.util;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilterIterator<E> implements Iterator<E> {
    private final Iterator<E> iterator;
    private final Predicate<E> filter;
    private E next;

    public FilterIterator(Iterator<E> iterator, Predicate<E> filter) {
        this.iterator = iterator;
        this.filter = filter;
    }

    @Override
    public boolean hasNext() {
        while (iterator.hasNext()) {
            next = iterator.next();
            if (filter.test(next)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E next() {
        return next;
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
