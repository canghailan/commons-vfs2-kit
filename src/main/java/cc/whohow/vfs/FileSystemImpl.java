package cc.whohow.vfs;

import org.apache.commons.vfs2.*;

import java.io.File;
import java.io.UncheckedIOException;

public interface FileSystemImpl extends FileSystem {
    @Override
    default FileObject getRoot() throws FileSystemException {
        return resolveFile(getRootURI());
    }

    @Override
    default String getRootURI() {
        return getRootName().getURI();
    }

    @Override
    default FileObject getParentLayer() throws FileSystemException {
        // default null
        return null;
    }

    @Override
    default Object getAttribute(String attrName) throws FileSystemException {
        throw new FileSystemException("vfs.provider/get-attribute-not-supported.error");
    }

    @Override
    default void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
    }

    @Override
    default FileObject resolveFile(FileName name) throws FileSystemException {
        return resolveFile(name.getURI());
    }

    @Override
    default void addListener(FileObject file, FileListener listener) {
        throw new UncheckedIOException(new FileSystemException("vfs.provider/notify-listener.warn", file));
    }

    @Override
    default void removeListener(FileObject file, FileListener listener) {
        throw new UncheckedIOException(new FileSystemException("vfs.provider/notify-listener.warn", file));
    }

    @Override
    default void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException {
        throw new FileSystemException("vfs.impl/create-junction.error", junctionPoint);
    }

    @Override
    default void removeJunction(String junctionPoint) throws FileSystemException {
        throw new FileSystemException("vfs.impl/create-junction.error", junctionPoint);
    }

    @Override
    default File replicateFile(FileObject file, FileSelector selector) throws FileSystemException {
        throw new FileSystemException("vfs.provider/replicate-file.error", file);
    }

    @Override
    default double getLastModTimeAccuracy() {
        // default
        return 0;
    }
}
