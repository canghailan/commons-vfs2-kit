package cc.whohow.configuration;

import cc.whohow.vfs.FileObject;

public interface FileBasedConfigurationManager {
    FileObject get(String key);
}
