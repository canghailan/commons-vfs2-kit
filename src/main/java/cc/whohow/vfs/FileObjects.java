package cc.whohow.vfs;

import cc.whohow.fs.util.FileReadableStream;
import cc.whohow.fs.util.FileWritableStream;
import cc.whohow.fs.util.IO;
import cc.whohow.fs.util.UncheckedCloseable;
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
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static boolean isFile(FileObject fileObject) {
        try {
            return fileObject.isFile();
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static boolean isFolder(FileObject fileObject) {
        try {
            return fileObject.isFolder();
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static long getSize(FileObject fileObject) {
        try {
            if (fileObject.isFolder()) {
                long size = 0;
                for (FileObject f : fileObject) {
                    if (f.isFile()) {
                        try (FileContent fileContent = fileObject.getContent()) {
                            size += fileContent.getSize();
                        }
                    }
                }
                return size;
            } else {
                try (FileContent fileContent = fileObject.getContent()) {
                    return fileContent.getSize();
                }
            }
        } catch (IOException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static long getLastModifiedTime(FileObject fileObject) {
        try (FileContent fileContent = fileObject.getContent()) {
            return fileContent.getLastModifiedTime();
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static FileObject[] list(FileObject fileObject) {
        try {
            return fileObject.getChildren();
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static Iterable<FileObject> listRecursively(FileObject fileObject) {
        return fileObject;
    }

    public static InputStream getInputStream(FileObject fileObject) {
        try {
            FileContent fileContent = fileObject.getContent();
            try {
                return new FileReadableStream(fileContent.getInputStream(), new UncheckedCloseable(fileContent));
            } catch (Exception e) {
                fileContent.close();
                throw e;
            }
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static OutputStream getOutputStream(FileObject fileObject) {
        try {
            FileContent fileContent = fileObject.getContent();
            try {
                return new FileWritableStream(fileContent.getOutputStream(), new UncheckedCloseable(fileContent));
            } catch (Exception e) {
                fileContent.close();
                throw e;
            }
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static ByteBuffer read(FileObject fileObject) {
        try (FileContent fileContent = fileObject.getContent()) {
            try (InputStream stream = fileContent.getInputStream()) {
                return IO.read(stream);
            }
        } catch (IOException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void write(FileObject fileObject, ByteBuffer buffer) {
        try (FileContent fileContent = fileObject.getContent()) {
            try (OutputStream stream = fileContent.getOutputStream()) {
                IO.write(stream, buffer);
            }
        } catch (IOException e) {
            throw FileSystemExceptions.unchecked(e);
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
