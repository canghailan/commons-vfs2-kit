package cc.whohow.fs;

import java.io.IOException;
import java.io.UncheckedIOException;

public class UncheckedFileSystemException extends RuntimeException {
    public UncheckedFileSystemException(String message) {
        super(message);
    }

    public UncheckedFileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncheckedFileSystemException(Throwable cause) {
        super(cause);
    }

    public static RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof IOException) {
            return new UncheckedIOException((IOException) e);
        }
        return new UncheckedFileSystemException(e);
    }
}
