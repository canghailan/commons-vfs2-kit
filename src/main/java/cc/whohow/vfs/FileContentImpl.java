package cc.whohow.vfs;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.time.FileTimes;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.cert.Certificate;
import java.util.Date;

public interface FileContentImpl extends FileContent {
    @Override
    default long getSize() throws FileSystemException {
        Number size = getAttributeAsNumber("size");
        if (size == null) {
            throw new FileSystemException("vfs.provider/get-size.error", this);
        }
        return size.longValue();
    }

    @Override
    default long getLastModifiedTime() throws FileSystemException {
        Date lastModifiedTime = getAttributeAsDate("lastModifiedTime");
        if (lastModifiedTime == null) {
            throw new FileSystemException("vfs.provider/get-last-modified.error", this);
        }
        return lastModifiedTime.getTime();
    }

    @Override
    default void setLastModifiedTime(long modTime) throws FileSystemException {
        setAttribute("lastModifiedTime", modTime);
    }

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
    default void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute.error", attrName, this);
    }

    @Override
    default void removeAttribute(String attrName) throws FileSystemException {
        throw new FileSystemException("vfs.provider/remove-attribute.error", attrName, this);
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
    default RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        throw new FileSystemException("vfs.provider/random-access-not-supported.error");
    }

    @Override
    default FileContentInfo getContentInfo() throws FileSystemException {
        return new DefaultFileContentInfo(
                getAttributeAsString("contentType"),
                getAttributeAsString("contentEncoding")
        );
    }

    @Override
    default boolean isOpen() {
        return false;
    }

    @Override
    default long write(FileContent output) throws IOException {
        try (OutputStream stream = output.getOutputStream()) {
            return write(stream);
        }
    }

    @Override
    default long write(FileObject file) throws IOException {
        try (FileContent content = file.getContent()) {
            return write(content);
        }
    }

    @Override
    default long write(OutputStream output) throws IOException {
        return write(output, 8 * 1024);
    }

    @Override
    default long write(OutputStream output, int bufferSize) throws IOException {
        try (InputStream input = getInputStream()) {
            long n = IO.transfer(input, output, bufferSize);
            output.flush();
            return n;
        }
    }

    default String getAttributeAsString(String attrName) throws FileSystemException {
        Object value = getAttribute(attrName);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    default Number getAttributeAsNumber(String attrName) throws FileSystemException {
        Object value = getAttribute(attrName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        return new BigDecimal(value.toString());
    }

    default Date getAttributeAsDate(String attrName) throws FileSystemException {
        Object value = getAttribute(attrName);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Number) {
            return FileTimes.fromMillis((Number) value);
        }
        return FileTimes.fromText(value.toString());
    }
}
