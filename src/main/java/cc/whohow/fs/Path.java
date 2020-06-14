package cc.whohow.fs;

import cc.whohow.vfs.path.PathParser;

import java.net.URI;

public interface Path extends Comparable<Path> {
    URI toUri();

    default String getName() {
        return new PathParser(toUri().getPath()).getLastName();
    }

    default String getExtension() {
        String name = getName();
        int index = name.lastIndexOf(name);
        // index == 0 不是扩展名
        if (index > 0) {
            return name.substring(index + 1);
        }
        return "";
    }

    default boolean isRegularFile() {
        return !isDirectory();
    }

    default boolean isDirectory() {
        return toUri().getPath().endsWith("/");
    }

    Path getParent();

    Path resolve(String relative);

    default String relativize(Path path) {
        URI relative = toUri().relativize(path.toUri());
        if (relative.isAbsolute() || relative.toString().startsWith("/")) {
            throw new IllegalArgumentException(path.toString());
        }
        return relative.toString();
    }

    default int compareTo(Path o) {
        return toUri().compareTo(o.toUri());
    }
}
