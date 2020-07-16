package cc.whohow.fs.util;

public class ComposeRunnable implements Runnable {
    protected final Runnable a;
    protected final Runnable b;

    public ComposeRunnable(Runnable a, Runnable b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @see java.util.stream.Streams#composeWithExceptions(java.lang.Runnable, java.lang.Runnable)
     */
    @Override
    public void run() {
        try {
            a.run();
        } catch (Throwable e1) {
            try {
                b.run();
            } catch (Throwable e2) {
                try {
                    e1.addSuppressed(e2);
                } catch (Throwable ignore) {
                }
            }
            throw e1;
        }
        b.run();
    }
}
