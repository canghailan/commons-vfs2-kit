package cc.whohow.fs.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.Iterator;

class DirectoryStreamAdapter<F> implements DirectoryStream<F> {
    private final Iterable<F> iterable;
    private final Closeable closeable;

    public DirectoryStreamAdapter(Iterable<F> iterable) {
        this(iterable, null);
    }

    public DirectoryStreamAdapter(Iterable<F> iterable, Closeable closeable) {
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
    public Iterator<F> iterator() {
        return iterable.iterator();
    }
}
