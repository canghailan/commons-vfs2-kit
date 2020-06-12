package cc.whohow.fs.util;

import cc.whohow.fs.FileIterator;
import cc.whohow.fs.FileStream;

import java.io.IOException;

@SuppressWarnings({"rawtypes", "unchecked"})
class EmptyFileStream<T> implements FileStream<T> {
    private static final EmptyFileStream INSTANCE = new EmptyFileStream();

    private EmptyFileStream() {
    }

    public static <T> EmptyFileStream<T> get() {
        return INSTANCE;
    }

    @Override
    public FileIterator<T> iterator() {
        return EmptyFileIterator.get();
    }

    @Override
    public void close() throws IOException {
    }
}
