package cc.whohow.fs.provider.memory;

import cc.whohow.fs.*;
import cc.whohow.fs.path.KeyPath;
import cc.whohow.fs.util.Files;
import cc.whohow.vfs.io.ByteBuffers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

public class MemoryFileSystem implements FileSystem<KeyPath, MemoryFile> {
    private static final Logger log = LogManager.getLogger(MemoryFileSystem.class);
    protected final URI uri;
    protected final FileSystemAttributes attributes;
    protected final NavigableMap<String, MemoryFile> storage;

    public MemoryFileSystem(URI uri) {
        this(uri, Files.emptyFileSystemAttributes());
    }

    public MemoryFileSystem(URI uri, FileSystemAttributes attributes) {
        this(uri, attributes, new ConcurrentSkipListMap<>());
    }

    public MemoryFileSystem(URI uri, FileSystemAttributes attributes, NavigableMap<String, MemoryFile> storage) {
        for (String key : storage.keySet()) {
            if (key.endsWith("/")) {
                throw new IllegalStateException();
            }
        }
        this.uri = uri;
        this.attributes = attributes;
        this.storage = storage;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public FileSystemAttributes readAttributes() {
        return attributes;
    }

    @Override
    public KeyPath resolve(CharSequence path) {
        String key = path.toString();
        if (key.startsWith("/")) {
            throw new IllegalArgumentException(key);
        }
        return new KeyPath(URI.create(uri + key), key);
    }

    @Override
    public boolean exists(KeyPath path) {
        if (path.isDirectory()) {
            return true;
        }
        return storage.containsKey(path.getKey());
    }

    @Override
    public MemoryFile get(KeyPath path) {
        if (path.isDirectory()) {
            return new MemoryFile(this, path);
        } else {
            MemoryFile file = storage.get(path.getKey());
            if (file != null) {
                return file;
            }
            log.debug("create new file: {}", path);
            return new MemoryRegularFile(this, path,
                    new BinaryObjectFile(path.toUri(), Files.emptyFileAttributes(), ByteBuffers.empty()));
        }
    }

    public void save(MemoryFile file) {
        if (file.isRegularFile()) {
            log.debug("save file: {}", file);
            storage.put(file.getPath().getKey(), file);
        }
    }

    @Override
    public FileAttributes readAttributes(KeyPath path) {
        MemoryFile file = storage.get(path.getKey());
        if (file == null) {
            throw new UncheckedIOException(new FileNotFoundException(path.toString()));
        }
        return file.readAttributes();
    }

    @Override
    public FileReadableChannel newReadableChannel(KeyPath path) {
        if (path.isRegularFile()) {
            MemoryFile file = storage.get(path.getKey());
            if (file == null) {
                throw new UncheckedIOException(new FileNotFoundException(path.toString()));
            }
            return file.newReadableChannel();
        }
        throw new IllegalArgumentException(path + "is not file");
    }

    @Override
    public FileWritableChannel newWritableChannel(KeyPath path) {
        if (path.isRegularFile()) {
            return get(path).newWritableChannel();
        }
        throw new IllegalArgumentException(path + "is not file");
    }

    @Override
    public DirectoryStream<MemoryFile> newDirectoryStream(KeyPath path) {
        if (path.isDirectory()) {
            Set<String> keys = new TreeSet<>();
            int begin = path.getKey().length();
            for (String key : storage.keySet()) {
                if (key.startsWith(path.getKey())) {
                    if (key.length() == begin) {
                        continue;
                    }
                    int end = key.indexOf('/', begin + 1);
                    if (end < 0) {
                        keys.add(key);
                    } else {
                        keys.add(key.substring(0, end + 1));
                    }
                }
            }
            List<MemoryFile> list = keys.stream()
                    .map(this::resolve)
                    .map(this::get)
                    .collect(Collectors.toList());
            return Files.newDirectoryStream(list);
        }
        throw new UnsupportedOperationException(path + " is not directory");
    }

    @Override
    public void delete(KeyPath path) {
        if (path.isDirectory()) {
            storage.keySet().stream()
                    .filter(path::isAncestorKey)
                    .collect(Collectors.toSet())
                    .forEach(storage::remove);
        } else {
            storage.remove(path.getKey());
        }
    }

    @Override
    public void close() throws Exception {

    }
}
