package cc.whohow.fs;

import java.io.IOException;
import java.io.UncheckedIOException;

public class UncheckedException extends RuntimeException {
    public UncheckedException(String message) {
        super(message);
    }

    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncheckedException(Throwable cause) {
        super(cause);
    }

    public static RuntimeException unchecked(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof IOException) {
            return new UncheckedIOException((IOException) e);
        }
        return new UncheckedException(e);
    }
}
