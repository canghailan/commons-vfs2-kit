package cc.whohow.vfs.io;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CloseHookOutputStream extends FilterOutputStream {
    protected final Runnable closeHook;

    public CloseHookOutputStream(OutputStream stream, Runnable closeHook) {
        super(stream);
        this.closeHook = closeHook;
    }

    public CloseHookOutputStream(OutputStream stream, Closeable closeable) {
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
