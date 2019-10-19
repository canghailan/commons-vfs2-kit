package cc.whohow.vfs.configuration;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

public class VirtualFileSystemConfigBuilder extends FileSystemConfigBuilder {
    private static final VirtualFileSystemConfigBuilder INSTANCE = new VirtualFileSystemConfigBuilder();

    public static VirtualFileSystemConfigBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return FileSystem.class;
    }

    @Override
    public void setParam(FileSystemOptions opts, String name, Object value) {
        super.setParam(opts, name, value);
    }

    @Override
    public Object getParam(FileSystemOptions opts, String name) {
        return super.getParam(opts, name);
    }
}
