package cc.whohow.vfs.io;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CloseHookInputStream extends FilterInputStream {
    protected final Runnable closeHook;

    public CloseHookInputStream(InputStream stream, Runnable closeHook) {
        super(stream);
        this.closeHook = closeHook;
    }

    public CloseHookInputStream(InputStream stream, Closeable closeable) {
        this(stream, new UncheckedCloseable(closeable));
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            closeHook.run();
        }
    }
}
