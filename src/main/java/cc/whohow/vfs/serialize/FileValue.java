package cc.whohow.vfs.serialize;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.util.Value;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class FileValue<T> implements Value<T> {
    protected final FileObjectX fileObject;
    protected final Serializer<T> serializer;

    public FileValue(FileObjectX fileObject, Serializer<T> serializer) {
        this.fileObject = fileObject;
        this.serializer = serializer;
    }

    public FileObjectX getFileObject() {
        return fileObject;
    }

    public Serializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public void watch(Consumer<T> callback) {
        fileObject.getFileSystem().addListener(fileObject, new FileListenerAdapter<>(this, callback));
    }

    @Override
    public void unwatch(Consumer<T> callback) {
        fileObject.getFileSystem().removeListener(fileObject, new FileListenerAdapter<>(this, callback));
    }

    @Override
    public void getAndWatch(Consumer<T> callback) {
        watch(callback);
        callback.accept(get());
    }

    @Override
    public void accept(T value) {
        try {
            serializer.serialize(fileObject, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public T get() {
        try {
            return serializer.deserialize(fileObject);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class Cache<T> extends FileValue<T> implements FileListener, AutoCloseable {
        protected volatile T value;

        public Cache(FileObjectX fileObject, Serializer<T> type) {
            super(fileObject, type);
            fileObject.getFileSystem().addListener(fileObject, this);
        }

        @Override
        public T get() {
            if (value == null) {
                value = doGet();
            }
            return value;
        }

        protected T doGet() {
            return super.get();
        }

        @Override
        public void accept(T value) {
            doAccept(value);
            this.value = null;
        }

        protected void doAccept(T value) {
            super.accept(value);
        }

        @Override
        public void close() {
            fileObject.getFileSystem().removeListener(fileObject, this);
        }

        @Override
        public void fileCreated(FileChangeEvent event) throws Exception {
            this.value = null;
        }

        @Override
        public void fileDeleted(FileChangeEvent event) throws Exception {
            this.value = null;
        }

        @Override
        public void fileChanged(FileChangeEvent event) throws Exception {
            this.value = null;
        }
    }

    public static class FileListenerAdapter<T> implements FileListener {
        protected FileValue<T> value;
        protected Consumer<T> callback;

        public FileListenerAdapter(FileValue<T> value, Consumer<T> callback) {
            this.value = value;
            this.callback = callback;
        }

        @Override
        public void fileCreated(FileChangeEvent event) throws Exception {
            callback.accept(value.get());
        }

        @Override
        public void fileDeleted(FileChangeEvent event) throws Exception {
            callback.accept(null);
        }

        @Override
        public void fileChanged(FileChangeEvent event) throws Exception {
            callback.accept(value.get());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof FileListenerAdapter) {
                FileListenerAdapter that = (FileListenerAdapter) o;
                return that.callback.equals(this.callback) &&
                        that.value.equals(this.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return callback.hashCode() * 31 + value.hashCode();
        }
    }
}
