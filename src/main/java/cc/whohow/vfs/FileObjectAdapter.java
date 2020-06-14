package cc.whohow.vfs;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.File;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.util.MappingIterator;
import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.selector.ImmutableFileSelectInfo;
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
    protected final FilePath path;

    public FileObjectAdapter(FilePath path) {
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
        log.trace("setLastModifiedTime({}): {}", modTime, this);
        throw new FileSystemException("");
    }

    @Override
    public OutputStream getOutputStream() throws FileSystemException {
        log.trace("getOutputStream: {}", this);
        return path.toFile().newWritableChannel().stream();
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        log.trace("getOutputStream({}): {}", bAppend, this);
        if (bAppend) {
            throw new FileSystemException("");
        }
        return getOutputStream();
    }

    @Override
    public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        log.trace("getRandomAccessContent({}): {}", mode, this);
        throw new FileSystemException("");
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
        log.trace("removeAttribute({}): {}", attrName, this);
        throw new FileSystemException("");
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        log.trace("setAttribute({}, {}): {}", attrName, value, this);
        throw new FileSystemException("");
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
        if (srcFile.isFolder()) {
            if (isFolder()) {
                log.trace("copyFrom[folder->folder]({}): -> {}", srcFile, this);
                DepthFirstFileTreeIterator iterator = new DepthFirstFileTreeIterator(srcFile, selector);
                try {
                    while (iterator.hasNext()) {
                        FileSelectInfo fileSelectInfo = iterator.next();
                        if (fileSelectInfo.getDepth() == 0) {
                            createFolder();
                            continue;
                        }
                        try (FileObject fileObject = fileSelectInfo.getFile()) {
                            try (FileObject newFileObject = resolveFile(
                                    fileSelectInfo.getBaseFolder().getName()
                                            .getRelativeName(fileSelectInfo.getFile().getName()))) {
                                if (fileObject.isFolder()) {
                                    newFileObject.createFolder();
                                } else {
                                    newFileObject.createFile();
                                    newFileObject.copyFrom(fileObject, Selectors.SELECT_SELF);
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
                            try (FileContent content = srcFile.getContent()) {
                                content.write(newFileObject);
                            }
                        }
                    } else {
                        log.trace("copyFrom[file->file]({}): -> {}", srcFile, this);
                        createFile();
                        try (FileContent content = srcFile.getContent()) {
                            content.write((FileContent) this);
                        }
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
        log.trace("createFile: {}", this);
    }

    @Override
    public void createFolder() throws FileSystemException {
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
        log.trace("findFiles({}): {}", depthwise, this);
        try {
            if (isFolder()) {
                if (depthwise) {
                    DepthFirstFileTreeIterator iterator = new DepthFirstFileTreeIterator(this, selector);
                    while (iterator.hasNext()) {
                        selected.add(iterator.next().getFile());
                    }
                } else {
                    BreadthFirstFileTreeIterator iterator = new BreadthFirstFileTreeIterator(this, selector);
                    if (iterator.hasNext()) {
                        selected.add(iterator.next().getFile());
                    }
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
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        log.trace("getChildren: {}", this);
        try (DirectoryStream<? extends File<?, ?>> stream = path.toFile().newDirectoryStream()) {
            List<FileObject> list = new ArrayList<>();
            for (File<?, ?> file : stream) {
                list.add(new FileObjectAdapter(new FilePath(path.getFileSystem(), file)));
            }
            return list.toArray(new FileObject[0]);
        } catch (IOException e) {
            throw FileSystemExceptions.rethrow(e);
        }
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        log.trace("getContent: {}", this);
        return this;
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        log.trace("getFileOperations: {}", this);
        return null;
    }

    @Override
    public FileSystem getFileSystem() {
        log.trace("getFileSystem: {}", this);
        return null;
    }

    @Override
    public FileName getName() {
        log.trace("getName: {}", this);
        return path;
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        log.trace("getParent: {}", this);
        return null;
    }

    @Override
    public String getPublicURIString() {
        log.trace("getPublicURIString: {}", this);
        return path.toFile().getPublicUri();
    }

    @Override
    public FileType getType() throws FileSystemException {
        log.trace("getType: {}", this);
        if (path.toFile().isDirectory()) {
            return FileType.FOLDER;
        } else {
            return FileType.FILE;
        }
    }

    @Override
    public URL getURL() throws FileSystemException {
        log.trace("getURL: {}", this);
        try {
            return new URL(path.toFile().getPublicUri());
        } catch (MalformedURLException e) {
            throw FileSystemExceptions.rethrow(e);
        }
    }

    @Override
    public boolean isAttached() {
        log.trace("isAttached: {}", this);
        return false;
    }

    @Override
    public boolean isContentOpen() {
        log.trace("isContentOpen: {}", this);
        return false;
    }

    @Override
    public boolean isExecutable() throws FileSystemException {
        log.trace("isExecutable: {}", this);
        return false;
    }

    @Override
    public boolean isFile() throws FileSystemException {
        log.trace("isFile: {}", this);
        return path.toFile().isRegularFile();
    }

    @Override
    public boolean isFolder() throws FileSystemException {
        log.trace("isFolder: {}", this);
        return path.toFile().isDirectory();
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
        fileObject.copyFrom(this, Selectors.SELECT_ALL);
        deleteAll();
    }

    @Override
    public void refresh() throws FileSystemException {
        log.trace("refresh: {}", this);
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        log.trace("resolveFile({}): {}", path, this);
        return null;
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        log.trace("resolveFile({}, {}): {}", name, scope, this);
        return null;
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        log.trace("setExecutable({}, {}): {}", executable, ownerOnly, this);
        return false;
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        log.trace("setReadable({}, {}): {}", readable, ownerOnly, this);
        return false;
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        log.trace("setWritable({}, {}): {}", writable, ownerOnly, this);
        return false;
    }

    @Override
    public int compareTo(FileObject o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public Iterator<FileObject> iterator() {
        log.trace("iterator: {}", this);
        return new MappingIterator<>(
                new DepthFirstFileTreeIterator(this, Selectors.EXCLUDE_SELF),
                FileSelectInfo::getFile);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof FileObjectAdapter) {
            FileObjectAdapter that = (FileObjectAdapter) o;
            return that.path.equals(this.path);
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
