package cc.whohow.vfs.util;

import java.io.Closeable;
import java.util.Iterator;
import java.util.function.Consumer;

public interface CloseableIterator<T> extends AutoCloseable, Iterator<T> {
    class Adapter<T> implements CloseableIterator<T> {
        private final Iterator<T> iterator;
        private final Closeable closeable;

        public Adapter(Iterator<T> iterator, Closeable closeable) {
            this.iterator = iterator;
            this.closeable = closeable;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public T next() {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }

        public void forEachRemaining(Consumer<? super T> action) {
            iterator.forEachRemaining(action);
        }

        @Override
        public void close() throws Exception {
            closeable.close();
        }
    }
}