package cc.whohow.vfs;

import cc.whohow.vfs.io.ResourceInputStream;
import cc.whohow.vfs.io.ResourceOutputStream;
import cc.whohow.vfs.provider.stream.StreamFileObject;
import cc.whohow.vfs.selector.AndFileSelector;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.operations.FileOperation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @see java.nio.file.Files
 */
public class FluentFileObject implements Closeable {
    private FileObject fileObject;

    public FluentFileObject(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public static FluentFileObject resolve(String name) {
        try {
            return resolve(VFS.getManager(), name);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FluentFileObject resolve(FileSystemManager fileSystemManager, String name) {
        try {
            return new FluentFileObject(fileSystemManager.resolveFile(name));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static long getSize(FileObject fileObject) {
        try (FileContent content = fileObject.getContent()) {
            return content.getSize();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject parent() {
        try (FileObject f = fileObject) {
            fileObject = f.getParent();
            return this;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject child(String name) {
        try (FileObject f = fileObject) {
            fileObject = f.getChild(name);
            return this;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject randomChild(String prefix, String suffix) {
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = "";
        }
        String name = prefix + UUID.randomUUID() + suffix;
        try (FileObject f = fileObject) {
            fileObject = f.getChild(name);
            fileObject.createFile();
            return this;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject randomChildLike(String uri) {
        // TODO
        return randomChild("", "");
    }

    @SuppressWarnings("unchecked")
    public <OP extends FileOperation> FluentFileObject apply(Class<OP> operation,
                                                             BiFunction<FileObject, OP, FileObject> function) {
        try {
            FileObject f = function.apply(fileObject, (OP) fileObject.getFileOperations().getOperation(operation));
            if (fileObject != f) {
                fileObject.close();
            }
            fileObject = f;
            return this;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public void close() {
        FileObjectFns.close(fileObject);
    }

    public FluentFileObject copyFrom(FileObject source) {
        try {
            fileObject.copyFrom(source, Selectors.SELECT_ALL);
            return this;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject copyFrom(String source) {
        try (FileObject f = fileObject.getFileSystem().getFileSystemManager().resolveFile(source)) {
            return copyFrom(f);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject copyTo(FileObject target) {
        try (FileObject f = fileObject) {
            target.copyFrom(f, Selectors.SELECT_ALL);
            fileObject = target;
            return this;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject copyTo(String target) {
        try {
            return copyTo(fileObject.getFileSystem().getFileSystemManager().resolveFile(target));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject transferFrom(InputStream stream) {
        return copyFrom(new StreamFileObject(stream));
    }

    public FluentFileObject transferTo(OutputStream stream) {
        try (FileContent fileContent = fileObject.getContent()) {
            fileContent.write(stream);
            return this;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FluentFileObject createFile() {
        FileObjectFns.createFile(fileObject);
        return this;
    }

    public FluentFileObject createFolder() {
        FileObjectFns.createFolder(fileObject);
        return this;
    }

    public FluentFileObject delete() {
        FileObjectFns.delete(fileObject);
        return this;
    }

    public FluentFileObject deleteQuietly() {
        FileObjectFns.deleteQuietly(fileObject);
        return this;
    }

    public FluentFileObject delete(FileSelector selector) {
        try {
            fileObject.delete(selector);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public FluentFileObject deleteQuietly(FileSelector selector) {
        try {
            fileObject.delete(selector);
        } catch (IOException ignore) {
        }
        return this;
    }

    public FluentFileObject deleteAll() {
        FileObjectFns.deleteAll(fileObject);
        return this;
    }

    public FluentFileObject deleteAllQuietly() {
        FileObjectFns.deleteAllQuietly(fileObject);
        return this;
    }

    public boolean exists() {
        return FileObjectFns.exists(fileObject);
    }

    public Stream<FileObject> list() {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.list();
            } else {
                return Arrays.stream(fileObject.getChildren());
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> list(FileFilter filter) {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.list(filter);
            } else {
                return Arrays.stream(fileObject.findFiles(
                        AndFileSelector.of(Selectors.SELECT_CHILDREN, new FileFilterSelector(filter))));
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> listRecursively() {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.listRecursively();
            } else {
                return Arrays.stream(fileObject.findFiles(Selectors.EXCLUDE_SELF));
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> listRecursively(FileSelector selector) {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.listRecursively(selector);
            } else {
                return Arrays.stream(fileObject.findFiles(AndFileSelector.of(Selectors.EXCLUDE_SELF, selector)));
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> listRecursively(FileSelector selector, boolean depthwise) {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.listRecursively(selector, depthwise);
            } else {
                List<FileObject> list = new ArrayList<>();
                fileObject.findFiles(AndFileSelector.of(Selectors.EXCLUDE_SELF, selector), depthwise, list);
                return list.stream();
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> find() {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.find();
            } else {
                return StreamSupport.stream(fileObject.spliterator(), false);
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> find(FileSelector selector) {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.find(selector);
            } else {
                return Arrays.stream(fileObject.findFiles(selector));
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<FileObject> find(FileSelector selector, boolean depthwise) {
        try {
            if (fileObject instanceof ListableFileObject) {
                ListableFileObject listableFileObject = (ListableFileObject) fileObject;
                return listableFileObject.find(selector, depthwise);
            } else {
                List<FileObject> list = new ArrayList<>();
                fileObject.findFiles(selector, depthwise, list);
                return list.stream();
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long getSize() {
        try {
            if (fileObject.isFolder()) {
                try (Stream<FileObject> list = listRecursively(Selectors.SELECT_FILES)) {
                    return list
                            .mapToLong(FluentFileObject::getSize)
                            .sum();
                }
            } else {
                return getSize(fileObject);
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getPublicURIString() {
        return fileObject.getPublicURIString();
    }

    /**
     * 获取输入流
     */
    public InputStream newInputStream(String uri) {
        try {
            FileContent fileContent = fileObject.getContent();
            return new ResourceInputStream(fileContent, fileContent.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 获取输出流
     */
    public OutputStream newOutputStream(FileObject fileObject) {
        try {
            FileContent fileContent = fileObject.getContent();
            return new ResourceOutputStream(fileContent, fileContent.getOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return fileObject.toString();
    }
}
