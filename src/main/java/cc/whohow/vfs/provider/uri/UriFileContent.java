package cc.whohow.vfs.provider.uri;

import cc.whohow.vfs.SimplifyFileContent;
import cc.whohow.vfs.StatelessFileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class UriFileContent implements SimplifyFileContent, StatelessFileContent {
    private final UriFileObject file;

    public UriFileContent(UriFileObject file) {
        this.file = file;
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    @Override
    public long getSize() throws FileSystemException {
        return 0;
    }

    @Override
    public long getLastModifiedTime() throws FileSystemException {
        return 0;
    }

    @Override
    public void setLastModifiedTime(long modTime) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
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
    public InputStream getInputStream() throws FileSystemException {
        try {
            return file.getURL().openStream();
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
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
    public FileContentInfo getContentInfo() throws FileSystemException {
        return null;
    }
}
