package cc.whohow.vfs.provider;

import cc.whohow.vfs.CloudFileSystem;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;

public class ConfFileSystemConfigBuilder extends FileSystemConfigBuilder {
    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return CloudFileSystem.class;
    }
}
