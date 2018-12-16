package cc.whohow.vfs;

import cc.whohow.vfs.selector.AndFileSelector;
import org.apache.commons.vfs2.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 可流式遍历文件
 */
public interface ListableFileObject extends FileObject {
    /**
     * 遍历文件
     */
    default Stream<FileObject> list() throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return find(Selectors.SELECT_CHILDREN);
    }

    /**
     * 遍历文件
     */
    default Stream<FileObject> list(FileFilter filter) throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return find(AndFileSelector.of(Selectors.SELECT_CHILDREN, new FileFilterSelector(filter)));
    }

    /**
     * 递归遍历文件
     */
    default Stream<FileObject> listRecursively() throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return find(Selectors.EXCLUDE_SELF);
    }

    /**
     * 递归遍历文件
     */
    default Stream<FileObject> listRecursively(FileSelector selector) throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return find(AndFileSelector.of(Selectors.EXCLUDE_SELF, selector));
    }

    /**
     * 递归遍历文件
     */
    default Stream<FileObject> listRecursively(FileSelector selector, boolean depthwise) throws FileSystemException {
        if (!isFolder()) {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
        return find(AndFileSelector.of(Selectors.EXCLUDE_SELF, selector), depthwise);
    }

    /**
     * 递归遍历文件，包含自身
     */
    default Stream<FileObject> find() throws FileSystemException {
        return find(Selectors.SELECT_ALL);
    }

    /**
     * 递归遍历文件，包含自身
     */
    default Stream<FileObject> find(FileSelector selector) throws FileSystemException {
        return find(selector, false);
    }

    /**
     * 递归遍历文件，包含自身
     */
    Stream<FileObject> find(FileSelector selector, boolean depthwise) throws FileSystemException;

    @Override
    default FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        try (Stream<FileObject> list = find(selector, true)) {
            return list.toArray(FileObject[]::new);
        }
    }

    @Override
    default void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        try (Stream<FileObject> list = find(selector, depthwise)) {
            list.forEach(selected::add);
        }
    }

    @Override
    default FileObject[] getChildren() throws FileSystemException {
        try (Stream<FileObject> list = list()) {
            return list.toArray(FileObject[]::new);
        }
    }

    @Override
    default Iterator<FileObject> iterator() {
        try {
            if (isFolder()) {
                try (Stream<FileObject> list = listRecursively()) {
                    return list.collect(Collectors.toList()).iterator();
                }
            } else {
                return Collections.emptyIterator();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
