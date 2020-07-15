package cc.whohow.vfs;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

public class FileSystemAdapterConfigBuilder extends FileSystemConfigBuilder {
    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return FileSystem.class;
    }

    @Override
    public void setParam(FileSystemOptions opts, String name, Object value) {
        super.setParam(opts, name, value);
    }

    @Override
    protected Object getParam(FileSystemOptions opts, String name) {
        return super.getParam(opts, name);
    }
}
