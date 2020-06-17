package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.fs.util.IO;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class AbstractFileBasedConfiguration<T> implements Configuration<T>, FileListener {
    private static final Logger log = LogManager.getLogger(AbstractFileBasedConfiguration.class);
    protected final FileObject fileObject;
    protected final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    protected volatile T value;

    public AbstractFileBasedConfiguration(FileObject fileObject) {
        this.fileObject = fileObject;
        this.fileObject.getFileSystem().addListener(this.fileObject, this);
    }

    @Override
    public void watch(Consumer<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void unwatch(Consumer<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public void getAndWatch(Consumer<T> listener) {
        watch(listener);
        listener.accept(get());
    }

    @Override
    public void close() throws Exception {
        try {
            listeners.clear();
            fileObject.getFileSystem().removeListener(fileObject, this);
        } finally {
            fileObject.close();
        }
    }

    @Override
    public synchronized void accept(T value) {
        try (FileContent fileContent = fileObject.getContent()) {
            try (OutputStream stream = fileContent.getOutputStream()) {
                IO.write(stream, serialize(value));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public T get() {
        if (value != null) {
            return value;
        }
        synchronized (this) {
            try (FileContent content = fileObject.getContent()) {
                return deserialize(IO.read(content.getInputStream()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        T oldValue = value;
        value = null;
        T newValue = get();
        if (!Objects.equals(oldValue, newValue)) {
            onChange(newValue);
        }
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        T oldValue = value;
        value = null;
        if (oldValue != null) {
            onChange(null);
        }
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        T oldValue = value;
        value = null;
        T newValue = get();
        if (!Objects.equals(oldValue, newValue)) {
            onChange(newValue);
        }
    }

    protected void onChange(T value) {
        for (Consumer<T> listener : listeners) {
            try {
                listener.accept(value);
            } catch (Exception e) {
                log.warn("configuration notify error", e);
            }
        }
    }

    protected abstract ByteBuffer serialize(T value) throws IOException;

    protected abstract T deserialize(ByteBuffer bytes) throws IOException;

    @Override
    public String toString() {
        return fileObject.toString();
    }
}
