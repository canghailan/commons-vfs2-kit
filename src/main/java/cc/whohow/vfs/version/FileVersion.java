package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;

public class FileVersion<V> {
    private final CloudFileObject fileObject;
    private final V version;

    public FileVersion(CloudFileObject fileObject, V version) {
        this.fileObject = fileObject;
        this.version = version;
    }

    public CloudFileObject getFileObject() {
        return fileObject;
    }

    public V getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return version + "\t" + fileObject.getName();
    }
}
