package cc.whohow.vfs;

import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.path.URIBuilder;
import cc.whohow.vfs.tree.FileObjectTree;
import cc.whohow.vfs.tree.FileObjectTreeIterator;
import cc.whohow.vfs.util.MapIterator;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * 轻量级文件对象
 */
public interface CloudFileObject extends FileObjectImpl, FileContentImpl {
    CloudFileSystem getFileSystem();

    DirectoryStream<CloudFileObject> list() throws FileSystemException;

    default DirectoryStream<CloudFileObject> listRecursively() throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return new FileObjectTree(this);
    }

    ReadableChannel getReadableChannel() throws FileSystemException;

    WritableChannel getWritableChannel() throws FileSystemException;

    @Override
    default CloudFileObject getParent() throws FileSystemException {
        FileName parent = getName().getParent();
        if (parent == null) {
            return null;
        }
        return resolveFile(parent.getURI());
    }

    @Override
    default CloudFileObject getChild(String name) throws FileSystemException {
        if (name.contains(org.apache.commons.vfs2.FileName.SEPARATOR)) {
            throw new IllegalArgumentException(name);
        }
        return resolveFile(name);
    }

    @Override
    default InputStream getInputStream() throws FileSystemException {
        return getReadableChannel();
    }

    @Override
    default OutputStream getOutputStream() throws FileSystemException {
        return getWritableChannel();
    }

    default List<String> getURIs() {
        return Collections.singletonList(getPublicURIString());
    }

    default <R> CloudFileOperation<CloudFileObject, R> getOperation(Class<? extends CloudFileOperation<CloudFileObject, R>> fileOperation) throws FileSystemException {
        return getFileOperations().getOperation(fileOperation, this);
    }

    @Override
    default CloudFileOperations getFileOperations() throws FileSystemException {
        return getFileSystem().getFileSystemProvider().getFileOperations();
    }

    @Override
    default CloudFileObject getFile() {
        return this;
    }

    @Override
    default CloudFileObject resolveFile(String path) throws FileSystemException {
        return getFileSystem().resolveFile(URIBuilder.resolve(getName().getURI(), path));
    }

    @Override
    default org.apache.commons.vfs2.FileObject[] getChildren() throws FileSystemException {
        try (DirectoryStream<CloudFileObject> list = list()) {
            return StreamSupport.stream(list.spliterator(), false).toArray(org.apache.commons.vfs2.FileObject[]::new);
        } catch (FileSystemException e) {
            throw e;
        } catch (IOException e) {
            throw new FileSystemException(e);
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
