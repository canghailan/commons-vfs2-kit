package cc.whohow.fs.util;

import cc.whohow.fs.FileIterator;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
class EmptyFileIterator<F> implements FileIterator<F> {
    private static final EmptyFileIterator INSTANCE = new EmptyFileIterator();

    private EmptyFileIterator() {
    }

    public static <T> EmptyFileIterator<T> get() {
        return INSTANCE;
    }

    @Override
    public void close() throws IOException {

    }

    public boolean hasNext() {
        return false;
    }

    public F next() {
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new IllegalStateException();
    }

    @Override
    public void forEachRemaining(Consumer<? super F> action) {
        Objects.requireNonNull(action);
    }
}
