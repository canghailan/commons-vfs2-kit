package cc.whohow.vfs;

import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.serialize.Serializer;
import org.apache.commons.vfs2.FileContent;
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

    public static long getSize(FileObject fileObject) {
        try {
            if (fileObject.isFolder()) {
                long size = 0;
                for (FileObject f : fileObject) {
                    if (f.isFile()) {
                        size += getFileSize(f);
                    }
                }
                return size;
            } else {
                return getFileSize(fileObject);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long getFileSize(FileObject fileObject) {
        try (FileContent fileContent = fileObject.getContent()) {
            return fileContent.getSize();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long getSize(FileObjectX fileObject) {
        try {
            if (fileObject.isFolder()) {
                try (DirectoryStream<FileObjectX> list = fileObject.listRecursively()) {
                    return StreamSupport.stream(list.spliterator(), false)
                            .filter(FileObjects::isFile)
                            .mapToLong(FileObjects::getFileSize)
                            .sum();
                }
            } else {
                return getFileSize(fileObject);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static long getFileSize(FileObjectX fileObject) {
        try {
            return fileObject.getSize();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long getLastModifiedTime(FileObjectX fileObject) {
        try {
            return fileObject.getLastModifiedTime();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileObject[] list(FileObject fileObject) {
        try {
            return fileObject.getChildren();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Iterable<FileObject> listRecursively(FileObject fileObject) {
        return fileObject;
    }

    public static DirectoryStream<FileObjectX> list(FileObjectX fileObject) {
        try {
            return fileObject.list();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static DirectoryStream<FileObjectX> listRecursively(FileObjectX fileObject) {
        try {
            return fileObject.listRecursively();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InputStream getInputStream(FileObjectX fileObject) {
        try {
            return fileObject.getInputStream();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static OutputStream getOutputStream(FileObjectX fileObject) {
        try {
            return fileObject.getOutputStream();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ByteBuffer read(FileObjectX fileObject) {
        try (ReadableChannel channel = fileObject.getReadableChannel()) {
            return channel.readAll();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(FileObjectX fileObject, ByteBuffer buffer) {
        try (WritableChannel channel = fileObject.getWritableChannel()) {
            channel.writeAll(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String read(FileObjectX fileObject, Charset charset) {
        return charset.decode(read(fileObject)).toString();
    }

    public static void write(FileObjectX fileObject, Charset charset, String text) {
        write(fileObject, charset.encode(text));
    }

    public static String readUtf8(FileObjectX fileObject) {
        return read(fileObject, StandardCharsets.UTF_8);
    }

    public static void writeUtf8(FileObjectX fileObject, String text) {
        write(fileObject, StandardCharsets.UTF_8, text);
    }

    public static <T> T read(FileObjectX fileObject, Serializer<T> serializer) {
        try {
            return serializer.deserialize(fileObject);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> void write(FileObjectX fileObject, Serializer<T> serializer, T value) {
        try {
            serializer.serialize(fileObject, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteAll(FileObject fileObject) {
        try {
            fileObject.deleteAll();
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
