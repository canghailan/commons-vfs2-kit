package cc.whohow.fs;

import cc.whohow.fs.util.Paths;

import java.net.URI;

public interface Path extends Comparable<Path> {
    URI toUri();

    default String getName() {
        return Paths.getName(toUri().getPath());
    }

    default String getExtension() {
        return Paths.getExtension(getName());
    }

    default boolean isRegularFile() {
        return !isDirectory();
    }

    default boolean isDirectory() {
        String path = toUri().getPath();
        return path == null || path.endsWith("/");
    }

    Path getParent();

    Path resolve(String relative);

    /**
     * @see java.nio.file.Path#startsWith(java.nio.file.Path)
     */
    default boolean startsWith(Path path) {
        return Paths.startsWith(toUri(), path.toUri());
    }

    /**
     * @see java.nio.file.Path#startsWith(java.lang.String)
     */
    default boolean startsWith(String path) {
        return Paths.startsWith(toUri(), URI.create(path));
    }

    default String relativize(Path path) {
        return Paths.relativize(toUri(), path.toUri());
    }

    default int compareTo(Path o) {
        return toUri().compareTo(o.toUri());
    }
}
