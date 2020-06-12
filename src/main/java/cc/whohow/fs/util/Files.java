package cc.whohow.fs.util;

import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileIterator;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.FileSystemAttributes;

import java.io.Closeable;
import java.nio.file.DirectoryStream;
import java.util.Iterator;

public class Files {
    public static FileAttributes emptyFileAttributes() {
        return EmptyFileAttributes.get();
    }

    public static FileSystemAttributes emptyFileSystemAttributes() {
        return EmptyFileSystemAttributes.get();
    }

    public static <F> FileIterator<F> emptyFileIterator() {
        return EmptyFileIterator.get();
    }

    public static <F> FileIterator<F> newFileIterator(Iterable<F> iterable) {
        return new FileIteratorAdapter<>(iterable.iterator());
    }

    public static <F> FileIterator<F> newFileIterator(Iterable<F> iterable, Closeable closeable) {
        return new FileIteratorAdapter<>(iterable.iterator(), closeable);
    }

    public static <F> FileIterator<F> newFileIterator(Iterator<F> iterator) {
        return new FileIteratorAdapter<>(iterator);
    }

    public static <F> FileIterator<F> newFileIterator(Iterator<F> iterator, Closeable closeable) {
        return new FileIteratorAdapter<>(iterator, closeable);
    }

    public static <F> FileStream<F> emptyFileStream() {
        return EmptyFileStream.get();
    }

    public static <F> FileStream<F> newFileStream(Iterable<F> iterable) {
        return new FileStreamAdapter<>(iterable);
    }

    public static <F> FileStream<F> newFileStream(Iterable<F> iterable, Closeable closeable) {
        return new FileStreamAdapter<>(iterable, closeable);
    }

    public static <F> DirectoryStream<F> emptyDirectoryStream() {
        return EmptyDirectoryStream.get();
    }

    public static <F> DirectoryStream<F> newDirectoryStream(Iterable<F> iterable) {
        return new DirectoryStreamAdapter<>(iterable);
    }

    public static <F> DirectoryStream<F> newDirectoryStream(Iterable<F> iterable, Closeable closeable) {
        return new DirectoryStreamAdapter<>(iterable, closeable);
    }
}
