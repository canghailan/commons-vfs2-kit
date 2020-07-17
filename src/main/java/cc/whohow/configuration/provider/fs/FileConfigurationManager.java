package cc.whohow.configuration.provider.fs;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.FileBasedConfigurationManager;
import cc.whohow.fs.File;
import cc.whohow.fs.FileEvent;
import cc.whohow.fs.FileListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;

public class FileConfigurationManager implements FileBasedConfigurationManager, FileListener {
    private static final Logger log = LogManager.getLogger(FileConfigurationManager.class);
    /**
     * 配置文件目录
     */
    protected final File root;

    public FileConfigurationManager(File root) {
        this.root = root;
        this.root.watch(this);
        log.debug("watch {}", root);
    }

    protected String getKey(File file) {
        return root.getPath().relativize(file.getPath());
    }

    @Override
    public List<String> list(String key) {
        try (DirectoryStream<? extends File> stream = root.resolve(key).newDirectoryStream()) {
            List<String> list = new ArrayList<>();
            for (File file : stream) {
                list.add(getKey(file));
            }
            return list;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Configuration<ByteBuffer> get(String key) {
        if (key.startsWith("/") || key.endsWith("/")) {
            throw new IllegalArgumentException(key);
        }
        return new FileConfigurationSource(root.resolve(key));
    }

    @Override
    public void close() throws Exception {
        root.unwatch(this);
        log.debug("unwatch {}", root);
    }

    @Override
    public void handleEvent(FileEvent event) throws Exception {
        log.debug("configuration changed: {} {}", event.kind(), event.file());
    }
}
