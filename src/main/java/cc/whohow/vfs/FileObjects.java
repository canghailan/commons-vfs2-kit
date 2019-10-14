package cc.whohow.vfs;

import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.type.DataType;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.util.stream.StreamSupport;

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

    public static long getSize(CloudFileObject fileObject) {
        try {
            if (fileObject.isFolder()) {
                try (DirectoryStream<CloudFileObject> list = fileObject.listRecursively()) {
                    return StreamSupport.stream(list.spliterator(), false)
                            .filter(FileObjects::isFile)
                            .mapToLong(FileObjects::getFileSize)
                            .sum();
                }
            } else {
                return fileObject.getSize();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static long getFileSize(CloudFileObject fileObject) {
        try {
            return fileObject.getSize();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long getLastModifiedTime(CloudFileObject fileObject) {
        try {
            return fileObject.getLastModifiedTime();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DirectoryStream<CloudFileObject> list(CloudFileObject fileObject) {
        try {
            return fileObject.list();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DirectoryStream<CloudFileObject> listRecursively(CloudFileObject fileObject) {
        try {
            return fileObject.listRecursively();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InputStream getInputStream(CloudFileObject fileObject) {
        try {
            return fileObject.getInputStream();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static OutputStream getOutputStream(CloudFileObject fileObject) {
        try {
            return fileObject.getOutputStream();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteBuffer read(CloudFileObject fileObject) {
        try (ReadableChannel channel = fileObject.getReadableChannel()) {
            return channel.readAll();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(CloudFileObject fileObject, ByteBuffer buffer) {
        try (WritableChannel channel = fileObject.getWritableChannel()) {
            channel.writeAll(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String read(CloudFileObject fileObject, Charset charset) {
        return charset.decode(read(fileObject)).toString();
    }

    public static void write(CloudFileObject fileObject, Charset charset, String text) {
        write(fileObject, charset.encode(text));
    }

    public static String readUtf8(CloudFileObject fileObject) {
        return read(fileObject, StandardCharsets.UTF_8);
    }

    public static void writeUtf8(CloudFileObject fileObject, String text) {
        write(fileObject, StandardCharsets.UTF_8, text);
    }

    public static <T> T read(CloudFileObject fileObject, DataType<T> type) {
        return new FileValue<>(fileObject, type).get();
    }

    public static <T> void write(CloudFileObject fileObject, DataType<T> type, T value) {
        new FileValue<>(fileObject, type).accept(value);
    }

    public static void delete(CloudFileObject fileObject) {
        try {
            fileObject.delete();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends FileOperation> T getOperation(FileObject fileObject, Class<T> fileOperation) {
        try {
            return (T) fileObject.getFileOperations().getOperation(fileOperation);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
