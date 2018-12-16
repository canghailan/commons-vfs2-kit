package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;

public interface ReadonlyFileObject extends FileObject {
    @Override
    default boolean canRenameTo(FileObject newfile) {
        return false;
    }

    @Override
    default void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default void createFile() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default void createFolder() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean delete() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default int delete(FileSelector selector) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default int deleteAll() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isReadable() throws FileSystemException {
        return true;
    }

    @Override
    default boolean isWriteable() throws FileSystemException {
        return false;
    }

    @Override
    default void moveTo(FileObject destFile) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        return readable && !ownerOnly;
    }

    @Override
    default boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        return false;
    }
}
