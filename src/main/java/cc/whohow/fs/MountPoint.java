package cc.whohow.fs;

import java.net.URI;
import java.util.Optional;

public interface MountPoint {
    String getPath();

    Optional<? extends File> resolve(URI uri);
}
