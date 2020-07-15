package cc.whohow.fs.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystemMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultVirtualFileSystemMetadata extends FileMetadata implements VirtualFileSystemMetadata {
    private static final Logger log = LogManager.getLogger(DefaultVirtualFileSystemMetadata.class);
    protected volatile Map<String, String> mountPoints;

    public DefaultVirtualFileSystemMetadata(File metadata) {
        super(metadata);
    }

    @Override
    public Map<String, String> getMountPoints() {
        if (mountPoints == null) {
            synchronized (this) {
                if (mountPoints == null) {
                    mountPoints = parseMountPoints();
                }
            }
        }
        return mountPoints;
    }

    protected Map<String, String> parseMountPoints() {
        log.debug("parseMountPoints");

        Map<String, String> mountPoints = new LinkedHashMap<>();
        File vfs = metadata.resolve("vfs");
        if (vfs.exists()) {
            for (String line : vfs.readUtf8().split("\n")) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] keyValue = line.split(":", 2);
                mountPoints.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return mountPoints;
    }
}
