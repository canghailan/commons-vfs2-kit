package cc.whohow.vfs.version;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileObjects;

public class FileLastModifiedTimeVersionProvider implements FileVersionProvider<Long> {
    @Override
    public FileVersion<Long> getVersion(FileObject fileObject) {
        return new FileVersion<>(fileObject, FileObjects.getLastModifiedTime(fileObject));
    }
}
