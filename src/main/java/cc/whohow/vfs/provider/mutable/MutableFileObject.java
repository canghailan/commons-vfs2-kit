package cc.whohow.vfs.provider.mutable;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileObjectList;
import cc.whohow.vfs.FileSystem;
import cc.whohow.vfs.tree.FileObjectListAdapter;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

public class MutableFileObject implements FileObject {
    private FileSystem fileSystem;
    private FileName name;
    private FileObject parent;
    private Map<String, FileObject> children = Collections.emptyMap();
    private Map<String, Object> attributes = Collections.emptyMap();
    private InputStream inputStream;
    private OutputStream outputStream;

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void setName(FileName name) {
        this.name = name;
    }

    public void setParent(FileObject parent) {
        this.parent = parent;
    }

    public void setChildren(Map<String, FileObject> children) {
        this.children = children;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
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
    public InputStream getInputStream() throws FileSystemException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        if (bAppend) {
            throw new FileSystemException("vfs.provider/write-append-not-supported.error");
        }
        return outputStream;
    }

    @Override
    public void close() throws FileSystemException {
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public FileName getName() {
        return name;
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        return parent;
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        if (children == null) {
            return null;
        }
        return children.get(name);
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        return null;
    }

    @Override
    public FileObjectList list() throws FileSystemException {
        return new FileObjectListAdapter(children.values());
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
