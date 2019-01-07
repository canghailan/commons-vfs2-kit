package cc.whohow.vfs;

import cc.whohow.vfs.io.Java9InputStream;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.Certificate;

public interface SimpleFileContent extends FileContent {
    @Override
    default boolean hasAttribute(String attrName) throws FileSystemException {
        return getAttributes().containsKey(attrName);
    }

    @Override
    default String[] getAttributeNames() throws FileSystemException {
        return getAttributes().keySet().toArray(new String[0]);
    }

    @Override
    default Object getAttribute(String attrName) throws FileSystemException {
        return getAttributes().get(attrName);
    }

    @Override
    default Certificate[] getCertificates() throws FileSystemException {
        return new Certificate[0];
    }

    @Override
    default OutputStream getOutputStream() throws FileSystemException {
        return getOutputStream(false);
    }

    @Override
    default long write(FileContent output) throws IOException {
        try (OutputStream stream = output.getOutputStream()) {
            return write(stream);
        }
    }

    @Override
    default long write(FileObject file) throws IOException {
        try (FileContent fileContent = file.getContent()) {
            return write(fileContent);
        }
    }

    @Override
    default long write(OutputStream output) throws IOException {
        return write(output, 8 * 1024);
    }

    @Override
    default long write(OutputStream output, int bufferSize) throws IOException {
        return new Java9InputStream(getInputStream()).transferTo(output, bufferSize);
    }
}
