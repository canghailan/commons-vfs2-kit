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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 配置文件基类
 */
public abstract class AbstractFileBasedConfiguration<T> implements Configuration<T>, FileListener {
    private static final Logger log = LogManager.getLogger(AbstractFileBasedConfiguration.class);
    /**
     * 配置文件
     */
    protected final FileObject fileObject;
    /**
     * 监听列表
     */
    protected final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    /**
     * 配置缓存
     */
    protected volatile T value;

    public AbstractFileBasedConfiguration(FileObject fileObject) {
        this.fileObject = fileObject;
        this.fileObject.getFileSystem().addListener(this.fileObject, this);
    }

    /**
     * 添加监听
     */
    @Override
    public void watch(Consumer<T> listener) {
        listeners.add(listener);
    }

    /**
     * 移除监听
     */
    @Override
    public void unwatch(Consumer<T> listener) {
        listeners.remove(listener);
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

    /**
     * 修改配置
     */
    @Override
    public synchronized void accept(T value) {
        try (FileContent fileContent = fileObject.getContent()) {
            try (OutputStream stream = fileContent.getOutputStream()) {
                IO.write(stream, serialize(value));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            this.value = null;
        }
    }

    /**
     * 读取配置
     */
    @Override
    public T get() {
        if (value != null) {
            return value;
        }
        synchronized (this) {
            if (value != null) {
                return value;
            }
            try (FileContent content = fileObject.getContent()) {
                return value = deserialize(IO.read(content.getInputStream()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * 清除缓存
     */
    protected T clear() {
        T value = this.value;
        this.value = null;
        return value;
    }

    /**
     * 配置文件创建回调
     */
    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
//        T oldValue = clear();
//        T newValue = get();
//        if (!Objects.equals(oldValue, newValue)) {
//            onChange(newValue);
//        }
        clear();
        onChange(get());
    }

    /**
     * 配置文件删除回调
     */
    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
//        T oldValue = clear();
//        if (oldValue != null) {
//            onChange(null);
//        }
        clear();
        onChange(null);
    }

    /**
     * 配置文件更新回调
     */
    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
//        T oldValue = clear();
//        T newValue = get();
//        if (!Objects.equals(oldValue, newValue)) {
//            onChange(newValue);
//        }
        clear();
        onChange(get());
    }

    /**
     * 配置变化回调
     */
    protected void onChange(T value) {
        for (Consumer<T> listener : listeners) {
            try {
                listener.accept(value);
            } catch (Exception e) {
                log.warn("configuration notify error", e);
            }
        }
    }

    /**
     * 序列化配置
     */
    protected abstract ByteBuffer serialize(T value) throws IOException;

    /**
     * 反序列化配置
     */
    protected abstract T deserialize(ByteBuffer bytes) throws IOException;

    @Override
    public String toString() {
        return fileObject.toString();
    }
}
