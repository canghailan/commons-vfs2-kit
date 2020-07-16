package cc.whohow.vfs;

import cc.whohow.fs.util.FileReadableStream;
import cc.whohow.fs.util.FileWritableStream;
import cc.whohow.fs.util.IO;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.operations.FileOperation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileObjects {
    public static FileObject resolveFile(String name) {
        try {
            return VFS.getManager().resolveFile(name);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

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

    public static void deleteAll(FileObject fileObject) {
        try {
            fileObject.deleteAll();
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends FileOperation> T getOperation(FileObject fileObject, Class<T> fileOperation) {
        try {
            return (T) fileObject.getFileOperations().getOperation(fileOperation);
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

    public static FileObject getParent(FileObject fileObject) {
        try {
            return fileObject.getParent();
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

    public static boolean isFile(FileObject fileObject) {
        try {
            return fileObject.isFile();
        } catch (FileSystemException e) {
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

    public static InputStream getInputStream(FileObject fileObject) {
        try {
            FileContent fileContent = fileObject.getContent();
            try {
                return new FileReadableStream(fileContent.getInputStream()) {
                    @Override
                    public synchronized void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            fileContent.close();
                        }
                    }
                };
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
                return new FileWritableStream(fileContent.getOutputStream()) {
                    @Override
                    public synchronized void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            fileContent.close();
                        }
                    }
                };
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

    public static void copy(FileObject source, FileObject target) {
        try {
            target.copyFrom(source, Selectors.SELECT_ALL);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void move(FileObject source, FileObject target) {
        try {
            source.moveTo(target);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    // --- URI API --- //

    public static FileName getName(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return fileObject.getName();
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static String getBaseName(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getBaseName(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static String getPublicURIString(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getPublicURIString(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static boolean exists(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return exists(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void deleteAll(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            deleteAll(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static <T extends FileOperation> T getOperation(String uri, Class<T> fileOperation) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getOperation(fileObject, fileOperation);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static long getSize(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getSize(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static boolean isFolder(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return isFolder(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static boolean isFile(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return isFile(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static long getLastModifiedTime(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getLastModifiedTime(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static InputStream getInputStream(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getInputStream(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static OutputStream getOutputStream(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return getOutputStream(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static ByteBuffer read(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return read(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void write(String uri, ByteBuffer buffer) {
        try (FileObject fileObject = resolveFile(uri)) {
            write(fileObject, buffer);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static String read(String uri, Charset charset) {
        try (FileObject fileObject = resolveFile(uri)) {
            return read(fileObject, charset);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void write(String uri, Charset charset, String text) {
        try (FileObject fileObject = resolveFile(uri)) {
            write(fileObject, charset, text);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static String readUtf8(String uri) {
        try (FileObject fileObject = resolveFile(uri)) {
            return readUtf8(fileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void writeUtf8(String uri, String text) {
        try (FileObject fileObject = resolveFile(uri)) {
            writeUtf8(fileObject, text);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void copy(String source, String target) {
        try (FileObject sourceFileObject = resolveFile(source);
             FileObject targetFileObject = resolveFile(target)) {
            copy(sourceFileObject, targetFileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    public static void move(String source, String target) {
        try (FileObject sourceFileObject = resolveFile(source);
             FileObject targetFileObject = resolveFile(target)) {
            move(sourceFileObject, targetFileObject);
        } catch (FileSystemException e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }
}
