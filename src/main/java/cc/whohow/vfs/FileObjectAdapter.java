package cc.whohow.vfs;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.File;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.util.IO;
import cc.whohow.fs.util.MappingIterator;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.security.cert.Certificate;
import java.util.*;

public class FileObjectAdapter implements FileObject, FileContent {
    private static final Logger log = LogManager.getLogger(FileObjectAdapter.class);
    protected FilePath path;

    public FileObjectAdapter(FilePath path) {
        Objects.requireNonNull(path);
        this.path = path;
    }

    @Override
    public boolean canRenameTo(FileObject fileObject) {
        log.trace("canRenameTo({}): <- {}", fileObject, this);
        return true;
    }

    @Override
    public void close() throws FileSystemException {
        log.trace("close: {}", this);
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        log.trace("getAttribute({}): {}", attrName, this);
        return path.toFile().readAttributes()
                .getValue(attrName)
                .orElse(null);
    }

    @Override
    public String[] getAttributeNames() throws FileSystemException {
        log.trace("getAttributeNames: {}", this);
        List<String> attributeNames = new ArrayList<>();
        for (Attribute<?> attribute : path.toFile().readAttributes()) {
            attributeNames.add(attribute.name());
        }
        return attributeNames.toArray(new String[0]);
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        log.trace("getAttributes: {}", this);
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (Attribute<?> attribute : path.toFile().readAttributes()) {
            attributes.put(attribute.name(), attribute.value());
        }
        return attributes;
    }

    @Override
    public Certificate[] getCertificates() throws FileSystemException {
        log.trace("getCertificates: {}", this);
        return new Certificate[0];
    }

    @Override
    public FileContentInfo getContentInfo() throws FileSystemException {
        log.trace("getContentInfo: {}", this);
        FileAttributes fileAttributes = path.toFile().readAttributes();
        Optional<String> contentType = fileAttributes.getAsString("Content-Type");
        Optional<String> contentEncoding = fileAttributes.getAsString("Content-Encoding");
        return new DefaultFileContentInfo(contentType.orElse(null), contentEncoding.orElse(null));
    }

    @Override
    public FileObject getFile() {
        log.trace("getFile: {}", this);
        return this;
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        log.trace("getInputStream: {}", this);
        return path.toFile().newReadableChannel().stream();
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        log.trace("getLastModifiedTime: {}", this);
        return path.toFile().readAttributes().lastModifiedTime().toMillis();
    }

    @Override
    public void setLastModifiedTime(long modTime) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
    }

    @Override
    public OutputStream getOutputStream() throws FileSystemException {
        log.trace("getOutputStream: {}", this);
        return path.toFile().newWritableChannel().stream();
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        if (bAppend) {
            throw new FileSystemException("vfs.provider/write-append-not-supported.error");
        }
        return getOutputStream();
    }

    @Override
    public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        throw new FileSystemException("vfs.provider/random-access-not-supported.error");
    }

    @Override
    public long getSize() throws FileSystemException {
        log.trace("getSize: {}", this);
        return path.toFile().size();
    }

    @Override
    public boolean hasAttribute(String attrName) throws FileSystemException {
        log.trace("hasAttribute({}): {}", attrName, this);
        return path.toFile().readAttributes().get(attrName).isPresent();
    }

    @Override
    public boolean isOpen() {
        log.trace("isOpen: {}", this);
        // stateless
        return false;
    }

    @Override
    public void removeAttribute(String attrName) throws FileSystemException {
        throw new FileSystemException("vfs.provider/remove-attribute-not-supported.error");
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
    }

    @Override
    public long write(FileContent output) throws IOException {
        log.trace("write({}): <- {}", output.getFile(), this);
        try (OutputStream stream = output.getOutputStream()) {
            return write(stream);
        }
    }

    @Override
    public long write(FileObject file) throws IOException {
        log.trace("write({}): <- {}", file, this);
        try (FileContent fileContent = file.getContent()) {
            return write(fileContent);
        }
    }

    @Override
    public long write(OutputStream output) throws IOException {
        log.trace("write: <- {}", this);
        return write(output, IO.BUFFER_SIZE);
    }

    @Override
    public long write(OutputStream output, int bufferSize) throws IOException {
        log.trace("write: <- {}", this);
        try (FileReadableChannel channel = path.toFile().newReadableChannel()) {
            return channel.transferTo(output);
        }
    }

    @Override
    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        if (srcFile.getType().hasChildren()) {
            if (isFolder()) {
                log.trace("copyFrom[folder->folder]({}): -> {}", srcFile, this);
                DepthFirstFileTreeIterator iterator = new DepthFirstFileTreeIterator(srcFile, selector);
                try {
                    FileObject baseFolder = this;
                    while (iterator.hasNext()) {
                        FileSelectInfo fileSelectInfo = iterator.next();
                        if (fileSelectInfo.getDepth() == 0) {
                            baseFolder = resolveFile(fileSelectInfo.getFile().getName().getBaseName() + "/");
                            baseFolder.createFolder();
                            continue;
                        }
                        try (FileObject fileObject = fileSelectInfo.getFile()) {
                            if (fileObject.isFolder()) {
                                try (FileObject newFileObject = baseFolder.resolveFile(
                                        fileSelectInfo.getBaseFolder().getName()
                                                .getRelativeName(fileObject.getName()))) {
                                    newFileObject.createFolder();
                                }
                            } else {
                                try (FileObject newFileObject = baseFolder.resolveFile(
                                        fileSelectInfo.getBaseFolder().getName()
                                                .getRelativeName(fileObject.getName()))) {
                                    newFileObject.createFile();
                                    try (FileContent content = newFileObject.getContent()) {
                                        content.write(fileObject);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    while (iterator.hasNext()) {
                        try {
                            iterator.next().getFile().close();
                        } catch (Exception ex) {
                            log.trace("close error", ex);
                        }
                    }
                    log.trace("copyFrom error", e);
                    throw FileSystemExceptions.rethrow(e);
                }
            } else {
                log.trace("copyFrom[folder->file]({}): -> {}", srcFile, this);
                throw new FileSystemException("vfs.provider/copy-file.error", srcFile, this);
            }
        } else {
            try {
                if (selector.includeFile(new ImmutableFileSelectInfo(null, this, 0))) {
                    if (isFolder()) {
                        log.trace("copyFrom[file->folder]({}): -> {}", srcFile, this);
                        try (FileObject newFileObject = resolveFile(srcFile.getName().getBaseName())) {
                            newFileObject.createFile();
                            try (FileContent content = newFileObject.getContent()) {
                                content.write(srcFile);
                            }
                        }
                    } else {
                        log.trace("copyFrom[file->file]({}): -> {}", srcFile, this);
                        createFile();
                        write(srcFile);
                    }
                }
            } catch (Exception e) {
                log.trace("copyFrom error", e);
                throw FileSystemExceptions.rethrow(e);
            }
        }
    }

    @Override
    public void createFile() throws FileSystemException {
        if (path.isFile()) {
            log.trace("createFile: {}", this);
        } else {
            throw new FileSystemException("vfs.provider/create-file.error", this);
        }
    }

    @Override
    public synchronized void createFolder() throws FileSystemException {
        if (path.isFile()) {
            File<?, ?> file = path.toFile();
            path = new FilePath(path.getFileSystem(), file.getFileSystem().get(file.getUri().resolve(file.getName() + "/")));
        }
        log.trace("createFolder: {}", this);
    }

    @Override
    public boolean delete() throws FileSystemException {
        log.trace("delete: {}", this);
        if (path.toFile().isDirectory()) {
            try (DirectoryStream<? extends File<?, ?>> stream = path.toFile().newDirectoryStream()) {
                if (stream.iterator().hasNext()) {
                    return false;
                }
                path.toFile().delete();
                return true;
            } catch (IOException e) {
                throw FileSystemExceptions.rethrow(e);
            }
        } else {
            path.toFile().delete();
            return true;
        }
    }

    @Override
    public int delete(FileSelector selector) throws FileSystemException {
        log.trace("delete: {}", this);
        if (isFolder()) {
            int n = 0;
            DepthFirstFileTreeIterator iterator = new DepthFirstFileTreeIterator(this, selector);
            Deque<FileObject> folders = new ArrayDeque<>();
            try {
                while (iterator.hasNext()) {
                    try (FileObject fileObject = iterator.next().getFile()) {
                        if (fileObject.isFolder()) {
                            folders.addLast(fileObject);
                        } else {
                            if (fileObject.delete()) {
                                n++;
                            }
                        }
                    }
                }
                while (!folders.isEmpty()) {
                    try {
                        if (folders.removeLast().delete()) {
                            n++;
                        }
                    } catch (Exception e) {
                        log.trace("delete folder error", e);
                    }
                }
                return n;
            } catch (Exception e) {
                while (iterator.hasNext()) {
                    try {
                        iterator.next().getFile().close();
                    } catch (Exception ex) {
                        log.trace("close error", ex);
                    }
                }
                while (!folders.isEmpty()) {
                    try {
                        folders.removeLast().close();
                    } catch (Exception ex) {
                        log.trace("close error", ex);
                    }
                }
                log.trace("delete error", e);
                throw FileSystemExceptions.rethrow(e);
            }
        } else {
            try {
                if (selector.includeFile(new ImmutableFileSelectInfo(null, this, 0))) {
                    path.toFile().delete();
                    return 1;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                log.trace("delete error", e);
                throw FileSystemExceptions.rethrow(e);
            }
        }
    }

    @Override
    public int deleteAll() throws FileSystemException {
        log.trace("deleteAll: {}", this);
        if (path.toFile().isDirectory()) {
            path.toFile().delete();
            return -1;
        } else {
            path.toFile().delete();
            return 1;
        }
    }

    @Override
    public boolean exists() throws FileSystemException {
        log.trace("exists: {}", this);
        return path.toFile().exists();
    }

    @Override
    public FileObject[] findFiles(FileSelector fileSelector) throws FileSystemException {
        log.trace("findFiles: {}", this);
        List<FileObject> list = new ArrayList<>();
        findFiles(fileSelector, true, list);
        return list.toArray(new FileObject[0]);
    }

    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        log.trace("findFiles({}): {}", depthwise ? "DepthFirst" : "BreadthFirst", this);
        try {
            if (isFolder()) {
                Iterator<FileSelectInfo> iterator = depthwise ?
                        new DepthFirstFileTreeIterator(this, selector) :
                        new BreadthFirstFileTreeIterator(this, selector);
                while (iterator.hasNext()) {
                    selected.add(iterator.next().getFile());
                }
            } else {
                if (selector.includeFile(new ImmutableFileSelectInfo(null, this, 0))) {
                    selected.add(this);
                }
            }
        } catch (Exception e) {
            throw FileSystemExceptions.rethrow(e);
        }
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        log.trace("getChild({}): {}", name, this);
        if (getType().hasChildren()) {
            try (DirectoryStream<? extends File<?, ?>> stream = path.toFile().newDirectoryStream()) {
                for (File<?, ?> file : stream) {
                    if (file.getName().equals(name)) {
                        return new FileObjectAdapter(new FilePath(path.getFileSystem(), file));
                    }
                }
                return null;
            } catch (IOException e) {
                throw FileSystemExceptions.rethrow(e);
            }
        } else {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        log.trace("getChildren: {}", this);
        if (getType().hasChildren()) {
            try (DirectoryStream<? extends File<?, ?>> stream = path.toFile().newDirectoryStream()) {
                List<FileObject> list = new ArrayList<>();
                for (File<?, ?> file : stream) {
                    list.add(new FileObjectAdapter(new FilePath(path.getFileSystem(), file)));
                }
                return list.toArray(new FileObject[0]);
            } catch (IOException e) {
                throw FileSystemExceptions.rethrow(e);
            }
        } else {
            throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
        }
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        return this;
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        log.trace("getFileOperations: {}", this);
        // TODO
        return null;
    }

    @Override
    public FileSystem getFileSystem() {
        return path.getFileSystem();
    }

    @Override
    public FileName getName() {
        return path;
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        FilePath parent = path.getParent();
        if (parent == null) {
            return null;
        }
        return new FileObjectAdapter(parent);
    }

    @Override
    public String getPublicURIString() {
        return path.toFile().getPublicUri();
    }

    @Override
    public FileType getType() throws FileSystemException {
        return path.getType();
    }

    @Override
    public URL getURL() throws FileSystemException {
        try {
            return new URL(path.toFile().getPublicUri());
        } catch (MalformedURLException e) {
            throw FileSystemExceptions.rethrow(e);
        }
    }

    @Override
    public boolean isAttached() {
        log.trace("isAttached: {}", this);
        // stateless
        return false;
    }

    @Override
    public boolean isContentOpen() {
        log.trace("isContentOpen: {}", this);
        // stateless
        return false;
    }

    @Override
    public boolean isExecutable() throws FileSystemException {
        log.trace("isExecutable: {}", this);
        return false;
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return getType() == FileType.FILE;
    }

    @Override
    public boolean isFolder() throws FileSystemException {
        return getType() == FileType.FOLDER;
    }

    @Override
    public boolean isHidden() throws FileSystemException {
        log.trace("isHidden: {}", this);
        return false;
    }

    @Override
    public boolean isReadable() throws FileSystemException {
        log.trace("isReadable: {}", this);
        return true;
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        log.trace("isWriteable: {}", this);
        return true;
    }

    @Override
    public void moveTo(FileObject fileObject) throws FileSystemException {
        log.trace("moveTo({}): <- {}", fileObject, this);
        fileObject.copyFrom(this, Selectors.EXCLUDE_SELF);
        deleteAll();
    }

    @Override
    public void refresh() throws FileSystemException {
        log.trace("refresh: {}", this);
        // stateless, do nothing
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        return resolveFile(path, NameScope.FILE_SYSTEM);
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        log.trace("resolveFile({}, {}): {}", name, scope, this);
        FileObject fileObject = getFileSystem().getFileSystemManager().resolveFile(this, name);
        switch (scope) {
            case FILE_SYSTEM: {
                return fileObject;
            }
            case CHILD: {
                if (equals(fileObject.getParent())) {
                    return fileObject;
                }
                throw new FileSystemException("vfs.provider/resolve-file.error", fileObject);
            }
            case DESCENDENT: {
                if (getName().isDescendent(fileObject.getName())) {
                    return fileObject;
                }
                throw new FileSystemException("vfs.provider/resolve-file.error", fileObject);
            }
            case DESCENDENT_OR_SELF: {
                if (equals(fileObject) || getName().isDescendent(fileObject.getName())) {
                    return fileObject;
                }
                throw new FileSystemException("vfs.provider/resolve-file.error", fileObject);
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-executable.error", this);
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-writeable.error", this);
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-readable.error", this);
    }

    @Override
    public int compareTo(FileObject o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public Iterator<FileObject> iterator() {
        log.trace("iterator: {}", this);
        try {
            return new MappingIterator<>(
                    new DepthFirstFileTreeIterator(this, Selectors.EXCLUDE_SELF),
                    FileSelectInfo::getFile);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof FileObjectAdapter) {
            FileObjectAdapter that = (FileObjectAdapter) o;
            return path.equals(that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
