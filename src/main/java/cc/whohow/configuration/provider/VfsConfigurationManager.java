package cc.whohow.configuration.provider;

import cc.whohow.configuration.FileBasedConfigurationManager;
import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.serialize.Serializer;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VfsConfigurationManager implements FileBasedConfigurationManager, FileListener {
    protected final CloudFileObject root;

    public VfsConfigurationManager(CloudFileObject root) {
        this.root = root;
        root.getFileSystem().addListener(root, this);
    }

    public CloudFileObject getRoot() {
        return root;
    }

    protected String getKey(CloudFileObject fileObject) {
        try {
            return root.getName().getRelativeName(fileObject.getName());
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
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
    public List<String> list(String key) {
        try (DirectoryStream<CloudFileObject> list = root.resolveFile(key).list()) {
            return StreamSupport.stream(list.spliterator(), false)
                    .map(this::getKey)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T get(String key, Serializer<T> serializer) {
        try {
            return serializer.deserialize(get(key));
        } catch (IOException e) {
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
