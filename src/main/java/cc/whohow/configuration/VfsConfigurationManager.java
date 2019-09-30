package cc.whohow.configuration;

import cc.whohow.vfs.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class VfsConfigurationManager implements FileBasedConfigurationManager {
    protected final FileObject fileObject;

    public VfsConfigurationManager(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public FileObject get(String key) {
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
