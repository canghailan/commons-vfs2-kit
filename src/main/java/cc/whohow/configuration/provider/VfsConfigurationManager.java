package cc.whohow.configuration.provider;

import cc.whohow.configuration.FileBasedConfigurationManager;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VfsConfigurationManager implements FileBasedConfigurationManager, FileListener {
    private static final Logger log = LogManager.getLogger(VfsConfigurationManager.class);
    protected final FileObject root;

    public VfsConfigurationManager(FileObject root) {
        this.root = root;
        this.root.getFileSystem().addListener(this.root, this);
    }

    protected String getKey(FileObject fileObject) {
        try {
            return root.getName().getRelativeName(fileObject.getName());
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileObject get(String key) {
        if (key.startsWith("/") || key.endsWith("/")) {
            throw new IllegalArgumentException();
        }
        try {
            return root.resolveFile(key);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<String> list(String key) {
        try {
            return Arrays.stream(root.resolveFile(key).getChildren())
                    .map(this::getKey)
                    .collect(Collectors.toList());
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        log.debug("fileCreated: {}", event.getFileObject());
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        log.debug("fileDeleted: {}", event.getFileObject());
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        log.debug("fileChanged: {}", event.getFileObject());
    }
}
