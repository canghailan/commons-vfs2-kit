package cc.whohow.vfs.version;

import cc.whohow.vfs.FileObjectX;

public class FileVersion<V> {
    private final FileObjectX fileObject;
    private final V version;

    public FileVersion(FileObjectX fileObject, V version) {
        this.fileObject = fileObject;
        this.version = version;
    }

    public FileObjectX getFileObject() {
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
