package cc.whohow.vfs;

import cc.whohow.vfs.type.DataType;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileObjects {
    public static String getBaseName(FileObject fileObject) {
        return fileObject.getName().getBaseName();
    }

    public static String getPublicURIString(FileObject fileObject) {
        return fileObject.getPublicURIString();
    }

    public static boolean exists(FileObject fileObject) {
        try {
            return fileObject.exists();
        } catch (FileNotFoundException e) {
            return false;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isFile(FileObject fileObject) {
        try {
            return fileObject.isFile();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isFolder(FileObject fileObject) {
        try {
            return fileObject.isFolder();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long getSize(FileObject fileObject) {
        try {
            if (fileObject.isFolder()) {
                try (FileObjectList list = fileObject.listRecursively()) {
                    return list.stream()
                            .filter(FileObjects::isFile)
                            .mapToLong(FileObjects::getFileSize)
                            .sum();
                }
            } else {
                return fileObject.getSize();
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static long getFileSize(FileObject fileObject) {
        try {
            return fileObject.getSize();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long getLastModifiedTime(FileObject fileObject) {
        try {
            return fileObject.getLastModifiedTime();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileObjectList list(FileObject fileObject) {
        try {
            return fileObject.list();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileObjectList listRecursively(FileObject fileObject) {
        try {
            return fileObject.listRecursively();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InputStream getInputStream(FileObject fileObject) {
        try {
            return fileObject.getInputStream();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static OutputStream getOutputStream(FileObject fileObject) {
        try {
            return fileObject.getOutputStream();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteBuffer read(FileObject fileObject) {
        try {
            return fileObject.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(FileObject fileObject, ByteBuffer buffer) {
        try {
            fileObject.write(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String read(FileObject fileObject, Charset charset) {
        return charset.decode(read(fileObject)).toString();
    }

    public static void write(FileObject fileObject, Charset charset, String text) {
        write(fileObject, charset.encode(text));
    }

    public static String readUtf8(FileObject fileObject) {
        return read(fileObject, StandardCharsets.UTF_8);
    }

    public static void writeUtf8(FileObject fileObject, String text) {
        write(fileObject, StandardCharsets.UTF_8, text);
    }

    public static <T> T read(FileObject fileObject, DataType<T> type) {
        try (InputStream stream = fileObject.getInputStream()) {
            return type.deserialize(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> void write(FileObject fileObject, DataType<T> type, T value) {
        try (OutputStream stream = fileObject.getOutputStream()) {
            type.serialize(stream, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void delete(FileObject fileObject) {
        try {
            fileObject.delete();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
