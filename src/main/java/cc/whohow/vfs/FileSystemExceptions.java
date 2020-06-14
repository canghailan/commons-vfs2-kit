package cc.whohow.vfs;

import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.UncheckedIOException;

public class FileSystemExceptions {
    public static FileSystemException rethrow(Throwable e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        if (e instanceof FileSystemException) {
            return (FileSystemException) e;
        }
        return new FileSystemException(e);
    }

    public static RuntimeException unchecked(FileSystemException e) {
        return new UncheckedIOException(e);
    }

    public static RuntimeException unchecked(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        if (e instanceof IOException) {
            return new UncheckedIOException((IOException) e);
        }
        return new UncheckedIOException(new FileSystemException(e));
    }
}
