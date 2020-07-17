package cc.whohow.fs.provider.file;

import cc.whohow.fs.*;
import cc.whohow.fs.util.Files;
import cc.whohow.fs.util.MappingIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class LocalFileSystem implements FileSystem<LocalPath, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileSystem.class);
    protected final URI uri;
    protected final FileSystemAttributes attributes;

    public LocalFileSystem(URI uri) {
        this(uri, Files.emptyFileSystemAttributes());
    }

    public LocalFileSystem(URI uri, FileSystemAttributes attributes) {
        this.uri = uri;
        this.attributes = attributes;
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
    public LocalPath resolve(URI uri) {
        if ("file".equals(uri.getScheme())) {
            return new LocalPath(uri);
        }
        throw new IllegalArgumentException(uri.toString());
    }

    @Override
    public LocalPath getParent(LocalPath path) {
        return null;
    }

    @Override
    public boolean exists(LocalPath path) {
        log.trace("Files.exists: {}", path);
        return java.nio.file.Files.exists(path.getFilePath());
    }

    public LocalFile get(java.nio.file.Path path) {
        return get(new LocalPath(path));
    }

    @Override
    public LocalFile get(LocalPath path) {
        return new LocalFile(this, path);
    }

    @Override
    public FileAttributes readAttributes(LocalPath path) {
        try {
            log.trace("Files.readAttributes: {}", path);
            return new LocalFileAttributes(java.nio.file.Files.getFileAttributeView(
                    path.getFilePath(), BasicFileAttributeView.class).readAttributes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileReadableChannel newReadableChannel(LocalPath path) {
        if (path.isRegularFile()) {
            try {
                log.trace("new FileInputStream: {}", path);
                return new LocalFileReadableChannel(new FileInputStream(path.getFilePath().toFile()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    @Override
    public FileWritableChannel newWritableChannel(LocalPath path) {
        if (path.isRegularFile()) {
            try {
                createDirectories(path);
                log.trace("new FileOutputStream: {}", path);
                return new LocalFileWritableChannel(new FileOutputStream(path.getFilePath().toFile()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    public void createDirectories(LocalPath path) {
        try {
            if (path.isDirectory()) {
                if (java.nio.file.Files.notExists(path.getFilePath())) {
                    java.nio.file.Files.createDirectories(path.getFilePath());
                }
            } else {
                java.nio.file.Path parent = path.getFilePath().getParent();
                if (parent != null) {
                    if (java.nio.file.Files.notExists(parent)) {
                        java.nio.file.Files.createDirectories(parent);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public DirectoryStream<LocalFile> newDirectoryStream(LocalPath path) {
        try {
            if (java.nio.file.Files.exists(path.getFilePath())) {
                log.trace("Files.newDirectoryStream: {}", path);
                DirectoryStream<java.nio.file.Path> directoryStream = java.nio.file.Files.newDirectoryStream(path.getFilePath());
                return Files.newDirectoryStream(new MappingIterable<>(directoryStream, this::get), directoryStream);
            } else {
                return Files.emptyDirectoryStream();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(LocalPath path) {
        try {
            if (java.nio.file.Files.notExists(path.getFilePath())) {
                return;
            }
            if (path.isDirectory()) {
                log.trace("Files.walkFileTree: {}", path);
                java.nio.file.Files.walkFileTree(path.getFilePath(), new SimpleFileVisitor<java.nio.file.Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        log.trace("Files.delete: {}", file);
                        java.nio.file.Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        log.trace("Files.delete: {}", dir);
                        java.nio.file.Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } else {
                log.trace("Files.delete: {}", path);
                java.nio.file.Files.delete(path.getFilePath());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public void close() throws Exception {
        log.debug("close LocalFileSystem: {}", this);
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
