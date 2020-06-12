package cc.whohow.fs.util;

import cc.whohow.fs.FileIterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

class FileIteratorAdapter<F> implements FileIterator<F> {
    private final Iterator<? extends F> iterator;
    private final Closeable closeable;

    public FileIteratorAdapter(Iterator<? extends F> iterator) {
        this(iterator, null);
    }

    public FileIteratorAdapter(Iterator<? extends F> iterator, Closeable closeable) {
        this.iterator = iterator;
        this.closeable = closeable;
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public F next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super F> action) {
        iterator.forEachRemaining(action);
    }
}
