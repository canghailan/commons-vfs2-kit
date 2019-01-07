package cc.whohow.vfs;

import cc.whohow.vfs.tree.FileObjectFindTree;
import cc.whohow.vfs.tree.TreeBreadthFirstIterator;
import cc.whohow.vfs.tree.TreePostOrderIterator;
import org.apache.commons.vfs2.*;

import java.util.Iterator;
import java.util.List;

public interface SimpleFileObject extends FileObject {
    @Override
    default boolean canRenameTo(FileObject newfile) {
        return true;
    }

    @Override
    default boolean delete() throws FileSystemException {
        if (!exists()) {
            return false;
        }
        if (isFolder() && getChildren().length > 0) {
            return false;
        }
        return delete(Selectors.SELECT_SELF) > 0;
    }

    @Override
    default int deleteAll() throws FileSystemException {
        return delete(Selectors.SELECT_ALL);
    }

    @Override
    default FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        return new FileObjectFindTree(this, selector, TreePostOrderIterator::new).stream()
                .map(FileSelectInfo::getFile)
                .toArray(FileObject[]::new);
    }

    @Override
    default void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        FileObjectFindTree tree = depthwise ?
                new FileObjectFindTree(this, selector, TreePostOrderIterator::new) :
                new FileObjectFindTree(this, selector, TreeBreadthFirstIterator::new);
        tree.stream()
                .map(FileSelectInfo::getFile)
                .forEach(selected::add);
    }

    @Override
    default FileObject getChild(String name) throws FileSystemException {
        for (FileObject fileObject : getChildren()) {
            if (fileObject.getName().getBaseName().equals(name)) {
                return this;
            }
        }
        return null;
    }

    @Override
    default FileType getType() throws FileSystemException {
        return getName().getType();
    }

    @Override
    default boolean isFile() throws FileSystemException {
        return getType() == FileType.FILE;
    }

    @Override
    default boolean isFolder() throws FileSystemException {
        return getType() == FileType.FOLDER;
    }

    @Override
    default void moveTo(FileObject destFile) throws FileSystemException {
        destFile.copyFrom(this, Selectors.SELECT_ALL);
        deleteAll();
    }

    @Override
    default FileObject resolveFile(String path) throws FileSystemException {
        return resolveFile(path, NameScope.FILE_SYSTEM);
    }

    @Override
    default int compareTo(FileObject o) {
        return getName().compareTo(o.getName());
    }

    @Override
    default Iterator<FileObject> iterator() {
        return new FileObjectFindTree(this, Selectors.EXCLUDE_SELF, TreeBreadthFirstIterator::new).stream()
                .map(FileSelectInfo::getFile)
                .iterator();
    }
}
