package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;

public class UncheckedCloseable implements Runnable {
    private final AutoCloseable closeable;

    public UncheckedCloseable(AutoCloseable closeable) {
        this.closeable = closeable;
    }

    @Override
    public void run() {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
