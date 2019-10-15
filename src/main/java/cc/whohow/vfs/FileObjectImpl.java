package cc.whohow.vfs;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.path.URIBuilder;
import cc.whohow.vfs.selector.FileSelectors;
import cc.whohow.vfs.selector.ImmutableFileSelectInfo;
import org.apache.commons.vfs2.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * FileObject 默认实现
 */
public interface FileObjectImpl extends FileObject {
    @Override
    default boolean canRenameTo(FileObject newfile) {
        return true;
    }

    @Override
    default void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        if (srcFile.isFolder()) {
            if (isFolder()) {
                // folder -> folder
                List<FileObject> list = new ArrayList<>();
                srcFile.findFiles(selector, true, list);
                try {
                    for (FileObject src : list) {
                        try (FileObject dst = resolveFile(srcFile.getName().getRelativeName(src.getName()))) {
                            if (src.isFolder()) {
                                dst.createFolder();
                            } else {
                                dst.createFile();
                                try (FileContent content = src.getContent()) {
                                    content.write(dst);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new FileSystemException(e);
                } finally {
                    IO.close(list);
                }
            } else {
                // folder -> file
                throw new FileSystemException("vfs.provider/copy-file.error", srcFile, this);
            }
        } else {
            try {
                if (FileSelectors.include(selector, srcFile)) {
                    if (isFolder()) {
                        // file -> folder
                        try (FileObject dst = getChild(srcFile.getName().getBaseName())) {
                            dst.createFile();
                            try (FileContent content = srcFile.getContent()) {
                                content.write(dst);
                            }
                        }
                    } else {
                        // file -> file
                        createFile();
                        try (FileContent content = srcFile.getContent()) {
                            content.write(this);
                        }
                    }
                }
            } catch (FileSystemException e) {
                throw e;
            } catch (IOException e) {
                throw new FileSystemException(e);
            }
        }
    }

    @Override
    default int delete(FileSelector selector) throws FileSystemException {
        List<org.apache.commons.vfs2.FileObject> list = new ArrayList<>();
        findFiles(selector, true, list);
        try {
            for (org.apache.commons.vfs2.FileObject fileObject : list) {
                fileObject.delete();
            }
            return list.size();
        } finally {
            IO.close(list);
        }
    }

    @Override
    default int deleteAll() throws FileSystemException {
        if (isFolder()) {
            FileObject[] list = getChildren();
            try {
                int n = 0;
                for (FileObject fileObject : list) {
                    if (fileObject.isFolder()) {
                        n += fileObject.deleteAll();
                    } else {
                        n += fileObject.delete() ? 1 : 0;
                    }
                }
                return n;
            } finally {
                IO.close(list);
            }
        } else {
            return delete() ? 1 : 0;
        }
    }

    @Override
    default FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        List<FileObject> list = new ArrayList<>();
        findFiles(selector, true, list);
        return list.toArray(new FileObject[0]);
    }

    @Override
    default void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        try {
            if (isFolder()) {
                if (depthwise) {
                    // Depth-First-Search
                    ArrayDeque<FileSelectInfo> stack = new ArrayDeque<>();
                    stack.push(new ImmutableFileSelectInfo(this, this, 0));
                    while (!stack.isEmpty()) {
                        FileSelectInfo fileSelectInfo = stack.pop();
                        FileObject fileObject = fileSelectInfo.getFile();
                        if (selector.includeFile(fileSelectInfo)) {
                            selected.add(fileObject);
                        } else {
                            IO.close(fileObject);
                        }
                        if (fileObject.isFolder() && selector.traverseDescendents(fileSelectInfo)) {
                            FileObject[] children = fileObject.getChildren();
                            for (int i = children.length - 1; i >= 0; i--) {
                                stack.push(new ImmutableFileSelectInfo(fileSelectInfo, children[i]));
                            }
                        }
                    }
                } else {
                    // Breadth-First-Search
                    ArrayDeque<FileSelectInfo> queue = new ArrayDeque<>();
                    queue.offer(new ImmutableFileSelectInfo(this, this, 0));
                    while (!queue.isEmpty()) {
                        FileSelectInfo fileSelectInfo = queue.poll();
                        FileObject fileObject = fileSelectInfo.getFile();
                        if (selector.includeFile(fileSelectInfo)) {
                            selected.add(fileObject);
                        } else {
                            IO.close(fileObject);
                        }
                        if (fileObject.isFolder() && selector.traverseDescendents(fileSelectInfo)) {
                            for (FileObject child : fileObject.getChildren()) {
                                queue.offer(new ImmutableFileSelectInfo(fileSelectInfo, child));
                            }
                        }
                    }
                }
            } else {
                if (selector.includeFile(new ImmutableFileSelectInfo(null, this, 0))) {
                    selected.add(this);
                }
            }
        } catch (FileSystemException e) {
            throw e;
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

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
        if (name.contains(FileName.SEPARATOR)) {
            throw new IllegalArgumentException(name);
        }
        return resolveFile(name);
    }

    @Override
    default String getPublicURIString() {
        // default public
        return getName().getFriendlyURI();
    }

    @Override
    default FileType getType() throws FileSystemException {
        // default by name
        return getName().getType();
    }

    @Override
    default URL getURL() throws FileSystemException {
        // default public
        try {
            return new URL(getPublicURIString());
        } catch (MalformedURLException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    default boolean isAttached() {
        // default stateless
        return false;
    }

    @Override
    default boolean isContentOpen() {
        // default stateless
        return false;
    }

    @Override
    default boolean isExecutable() throws FileSystemException {
        // default not executable
        return false;
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
    default boolean isHidden() throws FileSystemException {
        // default not hidden
        return false;
    }

    @Override
    default boolean isReadable() throws FileSystemException {
        // default readable
        return true;
    }

    @Override
    default boolean isWriteable() throws FileSystemException {
        // default writable
        return true;
    }

    @Override
    default void moveTo(FileObject destFile) throws FileSystemException {
        // default copy and delete
        destFile.copyFrom(this, Selectors.SELECT_ALL);
        deleteAll();
    }

    @Override
    default void refresh() throws FileSystemException {
        // default stateless
    }

    @Override
    default FileObject resolveFile(String path) throws FileSystemException {
        return getFileSystem().getFileSystemManager().resolveFile(URIBuilder.resolve(getName().getURI(), path));
    }

    @Override
    default FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        // default resolve and check
        FileObject fileObject = resolveFile(name);
        switch (scope) {
            case FILE_SYSTEM: {
                return fileObject;
            }
            case CHILD: {
                if (equals(fileObject.getParent())) {
                    return fileObject;
                }
            }
            case DESCENDENT: {
                if (getName().isDescendent(fileObject.getName())) {
                    return fileObject;
                }
            }
            case DESCENDENT_OR_SELF: {
                if (equals(fileObject) || getName().isDescendent(fileObject.getName())) {
                    return fileObject;
                }
            }
            default: {
                throw new FileSystemException("vfs.provider/resolve-file.error", name);
            }
        }
    }

    @Override
    default boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        // default ignore
        return false;
    }

    @Override
    default boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        // default ignore
        return false;
    }

    @Override
    default boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        // default ignore
        return false;
    }

    @Override
    default int compareTo(FileObject o) {
        // default compare by name
        return getName().compareTo(o.getName());
    }

    @Override
    default Iterator<FileObject> iterator() {
        // default by find
        try {
            ArrayList<FileObject> list = new ArrayList<>();
            findFiles(Selectors.EXCLUDE_SELF, true, list);
            return list.iterator();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
