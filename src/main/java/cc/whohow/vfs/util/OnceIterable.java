package cc.whohow.vfs.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class OnceIterable<T> implements Iterable<T> {
    private Iterator<T> iterator;

    public OnceIterable(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
