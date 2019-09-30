package cc.whohow.vfs.provider;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;

public class ConfFileSystemConfigBuilder extends FileSystemConfigBuilder {
    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return cc.whohow.vfs.FileSystem.class;
    }
}
