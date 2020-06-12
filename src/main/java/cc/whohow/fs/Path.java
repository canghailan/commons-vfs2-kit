package cc.whohow.fs;

import cc.whohow.vfs.path.PathParser;

import java.net.URI;

public interface Path extends Comparable<Path> {
    URI toUri();

    default String getName() {
        return new PathParser(toUri().getPath()).getLastName();
    }

    default boolean isRegularFile() {
        return !isDirectory();
    }

    default boolean isDirectory() {
        return toUri().getPath().endsWith("/");
    }

    default String relativize(Path path) {
        return toUri().relativize(path.toUri()).toString();
    }

    default int compareTo(Path o) {
        return toUri().compareTo(o.toUri());
    }
}
