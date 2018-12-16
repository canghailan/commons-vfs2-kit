package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.io.UncheckedIOException;
import java.net.URL;

public abstract class FileObjectFns {
    public static void close(FileObject fileObject) {
        try {
            fileObject.close();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void closeQuietly(FileObject fileObject) {
        try {
            fileObject.close();
        } catch (FileSystemException ignore) {
        }
    }

    public static void createFile(FileObject fileObject) {
        try {
            fileObject.createFile();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void createFolder(FileObject fileObject) {
        try {
            fileObject.createFolder();
        } catch (FileSystemException e) {
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

    public static void deleteQuietly(FileObject fileObject) {
        try {
            fileObject.delete();
        } catch (FileSystemException ignore) {
        }
    }

    public static void deleteAll(FileObject fileObject) {
        try {
            fileObject.deleteAll();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteAllQuietly(FileObject fileObject) {
        try {
            fileObject.deleteAll();
        } catch (FileSystemException ignore) {
        }
    }

    public static boolean exists(FileObject fileObject) {
        try {
            return fileObject.exists();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileObject[] getChildren(FileObject fileObject) {
        try {
            return fileObject.getChildren();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileObject getParent(FileObject fileObject) {
        try {
            return fileObject.getParent();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileType getType(FileObject fileObject) {
        try {
            return fileObject.getType();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static URL getURL(FileObject fileObject) {
        try {
            return fileObject.getURL();
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
}
