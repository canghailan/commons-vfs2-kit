package cc.whohow.vfs.util;

import cc.whohow.vfs.io.IO;

import java.util.Arrays;
import java.util.List;

public class CloseableList implements AutoCloseable, Runnable {
    private List<? extends AutoCloseable> closeable;

    public CloseableList(AutoCloseable... closeable) {
        this(Arrays.asList(closeable));
    }

    public CloseableList(List<? extends AutoCloseable> closeable) {
        this.closeable = closeable;
    }

    @Override
    public void close() {
        IO.close(closeable);
    }

    @Override
    public void run() {
        IO.close(closeable);
    }
}
