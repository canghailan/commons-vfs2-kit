package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;

import java.util.List;

/**
 * 可流式遍历文件
 */
public interface ListableFileObject extends FileObject {
    /**
     * 遍历文件
     */
    FileObjectList list() throws FileSystemException;
    /**
     * 遍历文件
     */
    FileObjectList list(FileSelector selector) throws FileSystemException;
    /**
     * 递归遍历文件
     */
    FileObjectList listRecursively() throws FileSystemException;
    /**
     * 递归遍历文件
     */
    FileObjectList listRecursively(FileSelector selector) throws FileSystemException;
    /**
     * 递归遍历文件
     */
    FileObjectList listRecursively(FileSelector selector, boolean depthwise) throws FileSystemException;
    /**
     * 递归遍历文件，包含自身
     */
    FileObjectList find() throws FileSystemException;
    /**
     * 递归遍历文件，包含自身
     */
    FileObjectList find(FileSelector selector) throws FileSystemException;
    /**
     * 递归遍历文件，包含自身
     */
    FileObjectList find(FileSelector selector, boolean depthwise) throws FileSystemException;

    @Override
    default FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        return find(selector).stream().toArray(FileObject[]::new);
    }

    @Override
    default void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        find(selector, depthwise).forEach(selected::add);
    }

    @Override
    default FileObject[] getChildren() throws FileSystemException {
        return list().stream().toArray(FileObject[]::new);
    }
}
