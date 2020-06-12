package cc.whohow.fs.util;

import cc.whohow.fs.FileIterator;
import cc.whohow.fs.FileStream;

import java.io.Closeable;
import java.io.IOException;

class FileStreamAdapter<F> implements FileStream<F> {
    private final Iterable<? extends F> iterable;
    private final Closeable closeable;

    public FileStreamAdapter(Iterable<? extends F> iterable) {
        this(iterable, null);
    }

    public FileStreamAdapter(Iterable<? extends F> iterable, Closeable closeable) {
        this.iterable = iterable;
        this.closeable = closeable;
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Override
    public FileIterator<F> iterator() {
        return new FileIteratorAdapter<>(iterable.iterator());
    }
}
