package cc.whohow.vfs.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;

public class UncheckedCloseable implements Runnable {
    private final Closeable closeable;

    public UncheckedCloseable(Closeable closeable) {
        this.closeable = closeable;
    }

    @Override
    public void run() {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
