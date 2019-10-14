package cc.whohow.vfs.provider.kv;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileSystem;
import cc.whohow.vfs.CloudFileSystemProvider;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.io.ByteBufferReadableChannel;
import cc.whohow.vfs.io.ByteBufferWritableChannel;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.path.PathBuilder;
import cc.whohow.vfs.path.URIBuilder;
import cc.whohow.vfs.provider.uri.UriFileName;
import cc.whohow.vfs.tree.FileObjectList;
import cc.whohow.vfs.type.DataType;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.util.*;

public class KeyValueFileObject<T> extends AbstractVfsComponent implements CloudFileObject, CloudFileSystem {
    protected final CloudFileSystemProvider fileSystemProvider;
    protected final UriFileName name;
    protected final DataType<T> type;
    protected final NavigableMap<String, T> data;

    public KeyValueFileObject(
            CloudFileSystemProvider fileSystemProvider,
            DataType<T> type,
            NavigableMap<String, T> data,
            UriFileName name) {
        this.fileSystemProvider = fileSystemProvider;
        this.type = type;
        this.data = data;
        this.name = name;
    }

    protected String getKey() {
        return name.getPathDecoded();
    }

    @Override
    public void createFile() throws FileSystemException {
        data.put(getKey(), null);
    }

    @Override
    public void createFolder() throws FileSystemException {
    }

    @Override
    public boolean delete() throws FileSystemException {
        if (isFile()) {
            return data.remove(getKey()) != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean exists() throws FileSystemException {
        return data.containsKey(getKey());
    }

    @Override
    public CloudFileSystem getFileSystem() {
        return this;
    }

    @Override
    public FileName getName() {
        return name;
    }

    @Override
    public DirectoryStream<CloudFileObject> list() throws FileSystemException {
        Set<String> paths = new TreeSet<>();
        int index = new PathBuilder(getKey()).getNameCount();
        for (String key : data.keySet()) {
            if (key.startsWith(getKey())) {
                PathBuilder path = new PathBuilder(key);
                String name = new PathBuilder(key).getName(index).toString();
                if (path.getNameCount() > index + 1 || path.endsWithSeparator()) {
                    paths.add(name + "/");
                } else {
                    paths.add(name);
                }
            }
        }
        List<CloudFileObject> list = new ArrayList<>(paths.size());
        for (String path : paths) {
            list.add(new KeyValueFileObject<>(fileSystemProvider, type, data,
                    new UriFileName(new URIBuilder(this.name.toURI()).setPath(getKey() + path).build())));
        }
        return new FileObjectList(list);
    }

    @Override
    public DirectoryStream<CloudFileObject> listRecursively() throws FileSystemException {
        List<CloudFileObject> list = new ArrayList<>();
        for (String key : data.keySet()) {
            if (key.startsWith(getKey())) {
                list.add(new KeyValueFileObject<>(fileSystemProvider, type, data,
                        new UriFileName(new URIBuilder(name.toURI()).setPath(key).build())));
            }
        }
        return new FileObjectList(list);
    }

    @Override
    public ReadableChannel getReadableChannel() throws FileSystemException {
        try {
            return new ByteBufferReadableChannel(type.serialize(data.get(getKey())));
        } catch (FileSystemException e) {
            throw e;
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public WritableChannel getWritableChannel() throws FileSystemException {
        return new ByteBufferWritableChannel() {
            @Override
            public void close() throws IOException {
                byteBuffer.flip();
                data.put(getKey(), type.deserialize(byteBuffer));
            }
        };
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        return Collections.emptyMap();
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        if (bAppend) {
            throw new FileSystemException("vfs.provider/write-append-not-supported.error");
        }
        return getWritableChannel();
    }

    public T get() {
        return data.get(getKey());
    }

    public void set(T value) {
        data.put(getKey(), value);
    }

    @Override
    public CloudFileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }

    @Override
    public VirtualFileSystem getFileSystemManager() {
        return (VirtualFileSystem) getContext().getFileSystemManager();
    }

    @Override
    public CloudFileObject resolveFile(String name) throws FileSystemException {
        return new KeyValueFileObject<>(fileSystemProvider, type, data, new UriFileName(this.name.toURI().resolve(name)));
    }

    @Override
    public FileName getRootName() {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute.error", attrName, this);
    }

    @Override
    public CloudFileObject resolveFile(FileName name) throws FileSystemException {
        return resolveFile(name.getURI());
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return getAttributes().get(attrName);
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
