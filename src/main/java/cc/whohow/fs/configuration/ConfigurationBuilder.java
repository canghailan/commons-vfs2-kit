package cc.whohow.fs.configuration;

import cc.whohow.fs.File;
import cc.whohow.fs.provider.ram.RamFileSystem;

import java.net.URI;

public class ConfigurationBuilder {
    protected File conf;
    protected StringBuilder vfs;

    public ConfigurationBuilder() {
        this(URI.create("meta:vfs:/"));
    }

    public ConfigurationBuilder(URI uri) {
        this(new RamFileSystem(uri).get(""));
    }

    public ConfigurationBuilder(File conf) {
        if (!conf.isDirectory()) {
            throw new IllegalArgumentException(conf.toString());
        }
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
        conf.resolve(path).writeUtf8(content);
        return this;
    }

    public File build() {
        conf.resolve("vfs").writeUtf8(vfs);
        return conf;
    }
}
