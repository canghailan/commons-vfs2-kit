package cc.whohow.vfs;

import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.operations.Move;
import cc.whohow.vfs.serialize.Serializer;
import org.apache.commons.vfs2.FileSystemException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理器
 */
public class VirtualFileManager {
    private final VirtualFileSystem fileSystem;

    public VirtualFileManager(VirtualFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public FileObjectX resolve(String path) {
        try {
            return fileSystem.resolveFile(path);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean exists(String path) {
        return FileObjects.exists(resolve(path));
    }

    public String getBaseName(String path) {
        return FileObjects.getBaseName(resolve(path));
    }

    public String getPublicURIString(String path) {
        return FileObjects.getPublicURIString(resolve(path));
    }

    public InputStream getInputStream(String path) {
        return FileObjects.getInputStream(resolve(path));
    }

    public OutputStream getOutputStream(String path) {
        return FileObjects.getOutputStream(resolve(path));
    }

    public ByteBuffer read(String path) {
        return FileObjects.read(resolve(path));
    }

    public void write(String path, ByteBuffer buffer) {
        FileObjects.write(resolve(path), buffer);
    }

    public String readUtf8(String path) {
        return StandardCharsets.UTF_8.decode(read(path)).toString();
    }

    public void writeUtf8(String path, String text) {
        write(path, StandardCharsets.UTF_8.encode(text));
    }

    public <T> T read(String path, Serializer<T> type) {
        return FileObjects.read(resolve(path), type);
    }

    public <T> void write(String path, Serializer<T> type, T value) {
        FileObjects.write(resolve(path), type, value);
    }

    public void deleteAll(String path) {
        FileObjects.deleteAll(resolve(path));
    }

    public void copy(String src, String dst) {
        try {
            fileSystem.copy(new Copy.Options(resolve(src), resolve(dst)));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void move(String src, String dst) {
        try {
            fileSystem.move(new Move.Options(resolve(src), resolve(dst)));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
