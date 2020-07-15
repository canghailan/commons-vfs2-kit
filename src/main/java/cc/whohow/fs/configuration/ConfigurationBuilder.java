package cc.whohow.fs.configuration;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.provider.ram.RamFileSystem;

import java.net.URI;

public class ConfigurationBuilder {
    protected FileSystem<?, ?> conf;
    protected StringBuilder vfs;

    public ConfigurationBuilder() {
        this(URI.create("meta:vfs:/"));
    }

    public ConfigurationBuilder(URI uri) {
        this(new RamFileSystem(uri));
    }

    public ConfigurationBuilder(FileSystem<?, ?> conf) {
        this.conf = conf;
        this.vfs = new StringBuilder();
    }

    public ConfigurationBuilder configureVfs(String path, String mounted) {
        if (path == null || path.isEmpty() || path.contains(":")) {
            throw new IllegalArgumentException(path);
        }
        if (mounted == null || mounted.isEmpty()) {
            throw new IllegalArgumentException(mounted);
        }
        vfs.append(path).append(": ").append(mounted).append("\n");
        return this;
    }

    public ConfigurationBuilder configure(String path, String content) {
        conf.get(path).writeUtf8(content);
        return this;
    }

    public File build() {
        conf.get("vfs").writeUtf8(vfs);
        return conf.get("");
    }
}
