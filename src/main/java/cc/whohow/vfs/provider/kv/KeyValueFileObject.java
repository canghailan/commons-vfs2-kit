package cc.whohow.vfs.provider.kv;

import cc.whohow.vfs.*;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.provider.uri.SimpleUriFileName;
import cc.whohow.vfs.type.DataType;
import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;

public class KeyValueFileObject<T> implements CloudFileObject, CloudFileSystem {
    protected final CloudFileSystemProvider fileSystemProvider;
    protected final DataType<T> type;
    protected final NavigableMap<String, T> data;
    protected final String key;

    public KeyValueFileObject(
            CloudFileSystemProvider fileSystemProvider,
            DataType<T> type,
            NavigableMap<String, T> data,
            String key) {
        this.fileSystemProvider = fileSystemProvider;
        this.type = type;
        this.data = data;
        this.key = key;
    }

    @Override
    public void createFile() throws FileSystemException {

    }

    @Override
    public void createFolder() throws FileSystemException {

    }

    @Override
    public boolean delete() throws FileSystemException {
        return false;
    }

    @Override
    public boolean exists() throws FileSystemException {
        return false;
    }

    @Override
    public CloudFileSystem getFileSystem() {
        return null;
    }

    @Override
    public FileName getName() {
        return new SimpleUriFileName("conf", key);
    }

    @Override
    public CloudFileObjectList list() throws FileSystemException {
        return null;
    }

    @Override
    public ReadableChannel getReadableChannel() throws FileSystemException {
        return null;
    }

    @Override
    public WritableChannel getWritableChannel() throws FileSystemException {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        return Collections.emptyMap();
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        return null;
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        return null;
    }

    public T get() {
        return data.get(key);
    }

    public void set(T value) {
        data.put(key, value);
    }

    @Override
    public void setLogger(Log logger) {

    }

    @Override
    public void setContext(VfsComponentContext context) {

    }

    @Override
    public void init() throws FileSystemException {

    }

    @Override
    public void close() {

    }

    @Override
    public CloudFileSystemProvider getFileSystemProvider() {
        return null;
    }

    @Override
    public VirtualFileSystem getFileSystemManager() {
        return null;
    }

    @Override
    public CloudFileObject resolve(CharSequence name) throws FileSystemException {
        return null;
    }

    @Override
    public FileName getRootName() {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {

    }

    @Override
    public CloudFileObject resolveFile(FileName name) throws FileSystemException {
        return null;
    }

    @Override
    public CloudFileObject resolveFile(String path) throws FileSystemException {
        return null;
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return null;
    }
}
