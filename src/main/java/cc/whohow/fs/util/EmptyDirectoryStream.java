package cc.whohow.fs.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.Collections;
import java.util.Iterator;

@SuppressWarnings({"rawtypes", "unchecked"})
class EmptyDirectoryStream<T> implements DirectoryStream<T> {
    private static final EmptyDirectoryStream INSTANCE = new EmptyDirectoryStream();

    private EmptyDirectoryStream() {
    }

    public static <T> EmptyDirectoryStream<T> get() {
        return INSTANCE;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void close() throws IOException {
    }
}
