package cc.whohow.configuration;

import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class VfsConfigurationManager implements FileBasedConfigurationManager {
    protected final CloudFileObject fileObject;

    public VfsConfigurationManager(CloudFileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public CloudFileObject get(String key) {
        if (key.startsWith("/") || key.endsWith("/")) {
            throw new IllegalArgumentException();
        }
        try {
            return fileObject.resolveFile(key);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
