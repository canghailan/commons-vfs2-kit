package cc.whohow.vfs;

import cc.whohow.vfs.type.DataType;
import cc.whohow.vfs.util.Value;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class FileValue<T> implements Value<T>, FileListener {
    protected final FileObject fileObject;
    protected final DataType<T> type;
    protected final Collection<Consumer<T>> callbacks = new CopyOnWriteArraySet<>();

    public FileValue(FileObject fileObject, DataType<T> type) {
        this.fileObject = fileObject;
        this.type = type;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public DataType<T> getType() {
        return type;
    }

    @Override
    public void watch(Consumer<T> callback) {
        if (callbacks.isEmpty()) {
            fileObject.getFileSystem().addListener(fileObject, this);
        }
        callbacks.add(callback);
    }

    @Override
    public void unwatch(Consumer<T> callback) {
        callbacks.remove(callback);
        if (callbacks.isEmpty()) {
            fileObject.getFileSystem().removeListener(fileObject, this);
        }
    }

    @Override
    public void accept(T value) {
        FileObjects.write(fileObject, type, value);
    }

    @Override
    public T get() {
        return FileObjects.read(fileObject, type);
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        notify(event);
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        // ignore
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        notify(event);
    }

    protected void notify(FileChangeEvent event) {
        T value = get();
        for (Consumer<T> callback : callbacks) {
            try {
                callback.accept(value);
            } catch (Exception ignore) {
            }
        }
    }

    public static class Cache<T> extends FileValue<T> implements AutoCloseable {
        protected volatile T value;

        public Cache(FileObject fileObject, DataType<T> type) {
            super(fileObject, type);
            fileObject.getFileSystem().addListener(fileObject, this);
        }

        @Override
        public T get() {
            if (value == null) {
                value = getImpl();
            }
            return value;
        }

        protected T getImpl() {
            return super.get();
        }

        @Override
        public void accept(T value) {
            acceptImpl(value);
            this.value = null;
        }

        protected void acceptImpl(T value) {
            super.accept(value);
        }

        @Override
        public void watch(Consumer<T> callback) {
            callbacks.add(callback);
        }

        @Override
        public void unwatch(Consumer<T> callback) {
            callbacks.remove(callback);
        }

        @Override
        protected void notify(FileChangeEvent event) {
            this.value = null;
            notifyImpl(event);
        }

        protected void notifyImpl(FileChangeEvent event) {
            super.notify(event);
        }

        @Override
        public void close() throws Exception {
            fileObject.getFileSystem().removeListener(fileObject, this);
        }
    }
}
