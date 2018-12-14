package cc.whohow.vfs;

import cc.whohow.vfs.io.Java9InputStream;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;
import org.apache.commons.vfs2.operations.DefaultFileOperations;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StreamFileObjectAdapter implements SimpleFileObject, FileContent {
    private final InputStream stream;

    public StreamFileObjectAdapter(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public boolean canRenameTo(FileObject newfile) {
        return false;
    }

    @Override
    public FileObject getFile() {
        return this;
    }

    @Override
    public long getSize() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastModifiedTime(long modTime) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttribute(String attrName) throws FileSystemException {
        return false;
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        return Collections.emptyMap();
    }

    @Override
    public String[] getAttributeNames() throws FileSystemException {
        return new String[0];
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String attrName) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Certificate[] getCertificates() throws FileSystemException {
        return new Certificate[0];
    }

    @Override
    public InputStream getInputStream() throws FileSystemException {
        return stream;
    }

    @Override
    public OutputStream getOutputStream() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws FileSystemException {
        try {
            stream.close();
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public FileContentInfo getContentInfo() throws FileSystemException {
        return new DefaultFileContentInfo(null, null);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public long write(FileContent output) throws IOException {
        try (OutputStream stream = output.getOutputStream()) {
            return write(stream);
        }
    }

    @Override
    public long write(FileObject file) throws IOException {
        try (FileContent fileContent = file.getContent()) {
            return write(fileContent);
        }
    }

    @Override
    public long write(OutputStream output) throws IOException {
        return write(output, 8 * 1024);
    }

    @Override
    public long write(OutputStream output, int bufferSize) throws IOException {
        return new Java9InputStream(stream).transferTo(output, bufferSize);
    }

    @Override
    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createFile() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createFolder() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete() throws FileSystemException {
        return true;
    }

    @Override
    public int delete(FileSelector selector) throws FileSystemException {
        return 1;
    }

    @Override
    public int deleteAll() throws FileSystemException {
        return 1;
    }

    @Override
    public boolean exists() throws FileSystemException {
        return true;
    }

    @Override
    public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        return this;
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        return new DefaultFileOperations(this);
    }

    @Override
    public FileSystem getFileSystem() {
        return null;
    }

    @Override
    public FileName getName() {
        return null;
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        return null;
    }

    @Override
    public String getPublicURIString() {
        return null;
    }

    @Override
    public FileType getType() throws FileSystemException {
        return FileType.FILE;
    }

    @Override
    public URL getURL() throws FileSystemException {
        return null;
    }

    @Override
    public boolean isAttached() {
        return true;
    }

    @Override
    public boolean isContentOpen() {
        return true;
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return true;
    }

    @Override
    public boolean isFolder() throws FileSystemException {
        return false;
    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public void moveTo(FileObject destFile) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh() throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(FileObject o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<FileObject> iterator() {
        return Collections.emptyIterator();
    }
}
