package cc.whohow.vfs.util;

import java.util.Collections;
import java.util.Iterator;

public class ConcatIterator<T> implements Iterator<T> {
    private Iterator<Iterator<T>> iterators;
    private Iterator<T> iterator = Collections.emptyIterator();

    public ConcatIterator(Iterator<Iterator<T>> iterators) {
        this.iterators = iterators;
    }

    @Override
    public boolean hasNext() {
        if (iterator.hasNext()) {
            return true;
        }
        while (iterators.hasNext()) {
            iterator = iterators.next();
            if (iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        return iterator.next();
    }
}
