package cc.whohow.fs;

import java.net.URI;
import java.util.Optional;

public interface MountPoint {
    String getPath();

    Optional<File> resolve(URI uri);
}
