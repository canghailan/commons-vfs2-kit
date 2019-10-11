package cc.whohow.configuration;

import cc.whohow.vfs.CloudFileObject;

public interface FileBasedConfigurationManager {
    CloudFileObject get(String key);
}
