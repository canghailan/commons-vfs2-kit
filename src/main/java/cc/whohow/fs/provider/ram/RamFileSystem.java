package cc.whohow.fs.provider.ram;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.KeyPath;
import cc.whohow.fs.util.ByteBuffers;
import cc.whohow.fs.util.Files;
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

public class RamFileSystem implements FileSystem<KeyPath, RamFile> {
    private static final Logger log = LogManager.getLogger(RamFileSystem.class);
    protected final URI uri;
    protected final FileSystemAttributes attributes;
    protected final NavigableMap<String, RamFile> storage;

    public RamFileSystem(URI uri) {
        this(uri, Files.emptyFileSystemAttributes());
    }

    public RamFileSystem(URI uri, FileSystemAttributes attributes) {
        this(uri, attributes, new ConcurrentSkipListMap<>());
    }

    public RamFileSystem(URI uri, FileSystemAttributes attributes, NavigableMap<String, RamFile> storage) {
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
    public KeyPath resolve(URI uri) {
        URI key = this.uri.relativize(uri);
        if (key.isAbsolute() || key.toString().startsWith("/")) {
            throw new IllegalArgumentException(uri.toString());
        }
        return new KeyPath(uri, key.toString());
    }

    @Override
    public KeyPath getParent(KeyPath path) {
        return null;
    }

    @Override
    public boolean exists(KeyPath path) {
        if (path.isDirectory()) {
            return true;
        }
        return storage.containsKey(path.getKey());
    }

    @Override
    public RamFile get(KeyPath path) {
        if (path.isDirectory()) {
            return new RamFile(this, path);
        } else {
            RamFile file = storage.get(path.getKey());
            if (file != null) {
                return file;
            }
            log.debug("create new file: {}", path);
            return new RamRegularFile(this, path,
                    new BinaryObjectFile(path.toUri(), Files.emptyFileAttributes(), ByteBuffers.empty()));
        }
    }

    public void save(RamFile file) {
        if (file.isRegularFile()) {
            log.debug("save file: {}", file);
            storage.put(file.getPath().getKey(), file);
        }
    }

    @Override
    public FileAttributes readAttributes(KeyPath path) {
        RamFile file = storage.get(path.getKey());
        if (file == null) {
            throw new UncheckedIOException(new FileNotFoundException(path.toString()));
        }
        return file.readAttributes();
    }

    @Override
    public FileReadableChannel newReadableChannel(KeyPath path) {
        if (path.isRegularFile()) {
            RamFile file = storage.get(path.getKey());
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
    public DirectoryStream<RamFile> newDirectoryStream(KeyPath path) {
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
            List<RamFile> list = keys.stream()
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

    @Override
    public String toString() {
        return uri.toString();
    }
}
