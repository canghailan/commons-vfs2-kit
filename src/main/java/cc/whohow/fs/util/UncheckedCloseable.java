package cc.whohow.fs.util;

import cc.whohow.fs.UncheckedException;

public class UncheckedCloseable implements Runnable {
    private final AutoCloseable closeable;

    public UncheckedCloseable(AutoCloseable closeable) {
        this.closeable = closeable;
    }

    @Override
    public void run() {
        try {
            closeable.close();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public String toString() {
        return "UncheckedCloseable(" + closeable + ")";
    }
}
