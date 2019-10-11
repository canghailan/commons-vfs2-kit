package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileObjects;

public class FileLastModifiedTimeVersionProvider implements FileVersionProvider<Long> {
    @Override
    public FileVersion<Long> getVersion(CloudFileObject fileObject) {
        return new FileVersion<>(fileObject, FileObjects.getLastModifiedTime(fileObject));
    }
}
