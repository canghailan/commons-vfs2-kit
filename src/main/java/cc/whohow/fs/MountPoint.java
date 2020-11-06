package cc.whohow.fs;

import java.net.URI;
import java.util.Optional;

/**
 * 虚拟文件系统挂载点
 */
public interface MountPoint {
    String getPath();

    Optional<File> resolve(URI uri);
}
