package cc.whohow.vfs.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class CompositeCloseable implements Closeable {
    private final LinkedList<Closeable> closeables = new LinkedList<>();

    public CompositeCloseable(Closeable... closeables) {
        this.closeables.addAll(Arrays.asList(closeables));
    }

    public CompositeCloseable compose(Closeable closeable) {
        closeables.addFirst(closeable);
        return this;
    }

    public CompositeCloseable andThen(Closeable closeable) {
        closeables.addLast(closeable);
        return this;
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (exception == null) {
                    exception =e;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
