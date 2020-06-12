package cc.whohow.fs.provider.file;

import cc.whohow.fs.FileSystem;
import cc.whohow.fs.*;
import cc.whohow.fs.channel.FileReadableStream;
import cc.whohow.fs.channel.FileWritableStream;
import cc.whohow.fs.util.Files;
import cc.whohow.fs.util.MappingIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.*;
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
    public LocalPath resolve(CharSequence path) {
        String ps = path.toString();
        java.nio.file.Path pp = Paths.get(ps);
        if (ps.endsWith(File.separator) || ps.endsWith("/")) {
            try {
                if (java.nio.file.Files.notExists(pp)) {
                    java.nio.file.Files.createDirectories(pp);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new LocalPath(pp);
    }

    @Override
    public LocalPath resolve(LocalPath base, CharSequence path) {
        return new LocalPath(base.getPath().resolve(path.toString()));
    }

    @Override
    public boolean exists(LocalPath path) {
        log.trace("exists: {}", path);
        return java.nio.file.Files.exists(path.getPath());
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
            log.trace("readAttributes: {}", path);
            return new LocalFileAttributes(java.nio.file.Files.getFileAttributeView(
                    path.getPath(), BasicFileAttributeView.class).readAttributes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileReadableChannel newReadableChannel(LocalPath path) {
        if (path.isRegularFile()) {
            try {
                // TODO FileChannel
                log.trace("newInputStream: {}", path);
                return new FileReadableStream(java.nio.file.Files.newInputStream(path.getPath()));
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
                java.nio.file.Path parent = path.getPath().getParent();
                log.trace("notExists: {}", parent);
                if (java.nio.file.Files.notExists(parent)) {
                    log.trace("createDirectories: {}", parent);
                    java.nio.file.Files.createDirectories(parent);
                }
                // TODO FileChannel
                log.trace("newOutputStream: {}", path);
                return new FileWritableStream(java.nio.file.Files.newOutputStream(path.getPath()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new UnsupportedOperationException(path + " is not file");
    }

    @Override
    public DirectoryStream<LocalFile> newDirectoryStream(LocalPath path) {
        try {
            log.trace("newDirectoryStream: {}", path);
            DirectoryStream<java.nio.file.Path> directoryStream = java.nio.file.Files.newDirectoryStream(path.getPath());
            return Files.newDirectoryStream(new MappingIterable<>(directoryStream, this::get), directoryStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(LocalPath path) {
        try {
            if (path.isDirectory()) {
                log.trace("walkFileTree: {}", path);
                java.nio.file.Files.walkFileTree(path.getPath(), new SimpleFileVisitor<java.nio.file.Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        log.trace("delete: {}", file);
                        java.nio.file.Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        log.trace("delete: {}", dir);
                        java.nio.file.Files.delete(dir);
                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } else {
                log.trace("delete: {}", path);
                java.nio.file.Files.delete(path.getPath());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public void close() throws Exception {
        log.debug("close LocalFileSystem: {}", uri);
    }
}
