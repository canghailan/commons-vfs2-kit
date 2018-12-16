package cc.whohow.vfs;

import org.apache.commons.vfs2.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 独立文件，文件间无关联
 */
public interface FreeFileObject extends FileObject {
    @Override
    default FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default FileObject getChild(String name) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default FileObject[] getChildren() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default FileSystem getFileSystem() {
        return FreeFileSystem.getInstance();
    }

    @Override
    default FileObject getParent() throws FileSystemException {
        return null;
    }

    @Override
    default FileType getType() throws FileSystemException {
        return FileType.FILE;
    }

    @Override
    default boolean isFile() throws FileSystemException {
        return true;
    }

    @Override
    default boolean isFolder() throws FileSystemException {
        return false;
    }

    @Override
    default FileObject resolveFile(String path) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    default Iterator<FileObject> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    default int compareTo(FileObject o) {
        return getName().compareTo(o.getName());
    }
}
