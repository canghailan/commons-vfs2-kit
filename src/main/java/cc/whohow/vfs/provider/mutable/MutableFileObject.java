package cc.whohow.vfs.provider.mutable;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileSystem;
import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.ReadableChannelAdapter;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.io.WritableChannelAdapter;
import cc.whohow.vfs.tree.FileObjectList;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.util.Collections;
import java.util.Map;

public class MutableFileObject implements CloudFileObject {
    private CloudFileSystem fileSystem;
    private FileName name;
    private CloudFileObject parent;
    private Map<String, CloudFileObject> children = Collections.emptyMap();
    private Map<String, Object> attributes = Collections.emptyMap();
    private ReadableChannel readableChannel;
    private WritableChannel writableChannel;

    public void setFileSystem(CloudFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void setName(FileName name) {
        this.name = name;
    }

    public void setParent(CloudFileObject parent) {
        this.parent = parent;
    }

    public void setChildren(Map<String, CloudFileObject> children) {
        this.children = children;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setInputStream(InputStream inputStream) {
        this.readableChannel = new ReadableChannelAdapter(inputStream);
    }

    public void setOutputStream(OutputStream outputStream) {
        this.writableChannel = new WritableChannelAdapter(outputStream);
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        return attributes;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        attributes.put(attrName, value);
    }

    @Override
    public void removeAttribute(String attrName) throws FileSystemException {
        attributes.remove(attrName);
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        if (bAppend) {
            throw new FileSystemException("vfs.provider/write-append-not-supported.error");
        }
        return writableChannel;
    }

    @Override
    public void close() throws FileSystemException {
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public CloudFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public FileName getName() {
        return name;
    }

    @Override
    public CloudFileObject getParent() throws FileSystemException {
        return parent;
    }

    @Override
    public CloudFileObject getChild(String name) throws FileSystemException {
        if (children == null) {
            return null;
        }
        return children.get(name);
    }

    @Override
    public DirectoryStream<CloudFileObject> list() throws FileSystemException {
        return new FileObjectList(children.values());
    }

    @Override
    public ReadableChannel getReadableChannel() throws FileSystemException {
        return readableChannel;
    }

    @Override
    public WritableChannel getWritableChannel() throws FileSystemException {
        return writableChannel;
    }

    @Override
    public boolean exists() throws FileSystemException {
        return true;
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
}
