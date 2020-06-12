package cc.whohow.fs.configuration;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.provider.memory.MemoryFileSystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

public class ConfigurationBuilder {
    protected FileSystem<?, ?> conf;
    protected WritableByteChannel vfs;

    public ConfigurationBuilder() {
        this(URI.create("etc:///"));
    }

    public ConfigurationBuilder(URI uri) {
        this(new MemoryFileSystem(uri));
    }

    public ConfigurationBuilder(FileSystem<?, ?> conf) {
        this.conf = conf;
        this.vfs = conf.get("vfs").newWritableChannel();
    }

    public ConfigurationBuilder configureVfs(String path, String mounted) {
        if (path == null || path.isEmpty() || path.contains(":")) {
            throw new IllegalArgumentException(path);
        }
        if (mounted == null || mounted.isEmpty()) {
            throw new IllegalArgumentException(mounted);
        }
        try {
            vfs.write(StandardCharsets.UTF_8.encode(path + ": " + mounted + "\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public ConfigurationBuilder configure(String path, String content) {
        conf.get(path).writeUtf8(content);
        return this;
    }

    public File<?, ?> build() {
        try {
            vfs.close();
            return conf.get("");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
