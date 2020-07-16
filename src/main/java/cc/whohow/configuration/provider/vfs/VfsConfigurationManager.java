package cc.whohow.configuration.provider.vfs;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.FileBasedConfigurationManager;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配置管理服务
 */
public class VfsConfigurationManager implements FileBasedConfigurationManager, FileListener {
    private static final Logger log = LogManager.getLogger(VfsConfigurationManager.class);
    /**
     * 配置文件目录
     */
    protected final FileObject root;

    public VfsConfigurationManager(FileObject root) {
        this.root = root;
        // 提前建立根目录监听
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
    public Configuration<ByteBuffer> get(String key) {
        if (key.startsWith("/") || key.endsWith("/")) {
            throw new IllegalArgumentException();
        }
        try {
            return new VfsConfigurationSource(root.resolveFile(key));
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

    @Override
    public void close() throws Exception {
        try {
            // 移除监听
            root.getFileSystem().removeListener(root, this);
        } finally {
            root.close();
        }
    }
}
