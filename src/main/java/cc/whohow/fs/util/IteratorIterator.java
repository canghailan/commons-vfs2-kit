package cc.whohow.fs.util;

import java.util.Collections;
import java.util.Iterator;

public class IteratorIterator<E> implements Iterator<E> {
    private final Iterator<? extends Iterator<? extends E>> iteratorIterator;
    private Iterator<? extends E> iterator;

    public IteratorIterator(Iterable<? extends Iterator<? extends E>> iteratorIterable) {
        this(iteratorIterable.iterator());
    }

    public IteratorIterator(Iterator<? extends Iterator<? extends E>> iteratorIterator) {
        this.iteratorIterator = iteratorIterator;
        this.iterator = Collections.emptyIterator();
    }

    @Override
    public boolean hasNext() {
        if (iterator.hasNext()) {
            return true;
        }
        while (iteratorIterator.hasNext()) {
            iterator = iteratorIterator.next();
            if (iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E next() {
        return iterator.next();
    }
}
