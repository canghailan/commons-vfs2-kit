package cc.whohow.vfs;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.tree.FileObjectTree;
import cc.whohow.vfs.tree.FileObjectTreeIterator;
import cc.whohow.vfs.util.MapIterator;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 轻量级文件对象
 */
public interface FileObject extends FileObjectImpl, FileContentImpl {
    FileSystem getFileSystem();

    FileName getName();

    FileObjectList list() throws FileSystemException;

    @Override
    default FileObject getParent() throws FileSystemException {
        FileName parent = getName().getParent();
        if (parent == null) {
            return null;
        }
        return resolveFile(parent.getURI());
    }

    @Override
    default FileObject getChild(String name) throws FileSystemException {
        if (name.contains(org.apache.commons.vfs2.FileName.SEPARATOR)) {
            throw new IllegalArgumentException(name);
        }
        return resolveFile(name);
    }

    default FileObjectList listRecursively() throws FileSystemException {
        return new FileObjectTree(this);
    }

    default ByteBuffer read() throws IOException {
        try (InputStream stream = getInputStream()) {
            return IO.read(stream);
        }
    }

    default void write(ByteBuffer buffer) throws IOException {
        try (OutputStream stream = getOutputStream()) {
            IO.write(stream, buffer);
        }
    }

    default List<String> getURIs() {
        return Collections.singletonList(getPublicURIString());
    }

    default <R> FileOperation<FileObject, R> getOperation(Class<? extends FileOperation<FileObject, R>> fileOperation) throws FileSystemException {
        return getFileOperations().getOperation(fileOperation, this);
    }

    @Override
    default FileOperations getFileOperations() throws FileSystemException {
        return getFileSystem().getFileSystemProvider().getFileOperations();
    }

    @Override
    default FileObject getFile() {
        return this;
    }

    @Override
    default FileObject resolveFile(String path) throws FileSystemException {
        return getFileSystem().resolveFile(path);
    }

    @Override
    default org.apache.commons.vfs2.FileObject[] getChildren() throws FileSystemException {
        try (FileObjectList list = list()) {
            return list.stream().toArray(org.apache.commons.vfs2.FileObject[]::new);
        }
    }

    @Override
    default FileContent getContent() throws FileSystemException {
        return this;
    }

    @Override
    default void close() throws FileSystemException {
        // default stateless
    }

    @Override
    default Iterator<org.apache.commons.vfs2.FileObject> iterator() {
        return new MapIterator<>(new FileObjectTreeIterator(this), fileObject -> fileObject);
    }
}
