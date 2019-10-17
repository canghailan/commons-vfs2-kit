package cc.whohow.configuration.provider;

import cc.whohow.configuration.FileBasedConfigurationManager;
import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class VfsConfigurationManager implements FileBasedConfigurationManager, FileListener {
    protected final CloudFileObject root;

    public VfsConfigurationManager(CloudFileObject root) {
        this.root = root;
        root.getFileSystem().addListener(root, this);
    }

    public CloudFileObject getRoot() {
        return root;
    }

    @Override
    public CloudFileObject get(String key) {
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
    public void fileCreated(FileChangeEvent event) throws Exception {

    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {

    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {

    }
}
