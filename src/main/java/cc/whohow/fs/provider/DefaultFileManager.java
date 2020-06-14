package cc.whohow.fs.provider;

import cc.whohow.fs.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DefaultFileManager implements FileManager {
    protected final VirtualFileSystem vfs;

    public DefaultFileManager(VirtualFileSystem vfs) {
        this.vfs = vfs;
    }

    @Override
    public String resolve(String... paths) {
        return String.join("/", paths);
    }

    @Override
    public String getUri(String uri) {
        return vfs.get(uri).getUri().toString();
    }

    @Override
    public String getPublicUri(String uri) {
        return vfs.get(uri).getPublicUri();
    }

    @Override
    public Collection<String> getUris(String uri) {
        return vfs.get(uri).getUris();
    }

    @Override
    public String getName(String uri) {
        return vfs.get(uri).getName();
    }

    @Override
    public String getExtension(String uri) {
        String name = getName(uri);
        int index = name.lastIndexOf(name);
        // index == 0 不是扩展名
        if (index > 0) {
            return name.substring(index + 1);
        }
        return "";
    }

    @Override
    public boolean exists(String uri) {
        return vfs.get(uri).exists();
    }

    @Override
    public boolean isRegularFile(String uri) {
        return vfs.get(uri).isRegularFile();
    }

    @Override
    public boolean isDirectory(String uri) {
        return vfs.get(uri).isDirectory();
    }

    @Override
    public FileAttributes readAttributes(String uri) {
        return vfs.get(uri).readAttributes();
    }

    @Override
    public FileSystemAttributes readFileSystemAttributes(String uri) {
        return vfs.get(uri).getFileSystem().readAttributes();
    }

    @Override
    public long size(String uri) {
        return vfs.get(uri).size();
    }

    @Override
    public FileTime getLastModifiedTime(String uri) {
        return vfs.get(uri).getLastModifiedTime();
    }

    @Override
    public InputStream newInputStream(String uri) {
        return vfs.get(uri).newReadableChannel().stream();
    }

    @Override
    public OutputStream newOutputStream(String uri) {
        return vfs.get(uri).newWritableChannel().stream();
    }

    @Override
    public ReadableByteChannel newReadableChannel(String uri) {
        return vfs.get(uri).newReadableChannel();
    }

    @Override
    public WritableByteChannel newWritableChannel(String uri) {
        return vfs.get(uri).newWritableChannel();
    }

    @Override
    public ByteBuffer read(String uri) {
        return vfs.get(uri).read();
    }

    @Override
    public void write(String uri, ByteBuffer content) {
        vfs.get(uri).write(content);
    }

    @Override
    public String read(String uri, Charset charset) {
        return vfs.get(uri).read(charset);
    }

    @Override
    public void write(String uri, CharSequence content, Charset charset) {
        vfs.get(uri).write(content, charset);
    }

    @Override
    public String readUtf8(String uri) {
        return vfs.get(uri).readUtf8();
    }

    @Override
    public void writeUtf8(String uri, CharSequence content) {
        vfs.get(uri).writeUtf8(content);
    }

    @Override
    public void delete(String uri) {
        vfs.get(uri).delete();
    }

    @Override
    public void deleteQuietly(String uri) {
        try {
            delete(uri);
        } catch (Throwable ignore) {
        }
    }

    @Override
    public String copy(String source, String destination) {
        try {
            return vfs.newCopyCommand(source, destination).call().getPublicUri();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public CompletableFuture<String> copyAsync(String source, String destination) {
        try {
            return CompletableFuture.supplyAsync(vfs.newCopyCommand(source, destination), vfs.getExecutor())
                    .thenApply(File::getPublicUri);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public String move(String source, String destination) {
        try {
            return vfs.newMoveCommand(source, destination).call().getPublicUri();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public CompletableFuture<String> moveAsync(String source, String destination) {
        try {
            return CompletableFuture.supplyAsync(vfs.newMoveCommand(source, destination), vfs.getExecutor())
                    .thenApply(File::getPublicUri);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T exec(String... args) {
        try {
            return (T) vfs.newCommand(args).call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> execAsync(String... args) {
        try {
            return (CompletableFuture<T>) CompletableFuture.supplyAsync(vfs.newCommand(args), vfs.getExecutor());
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }
}
