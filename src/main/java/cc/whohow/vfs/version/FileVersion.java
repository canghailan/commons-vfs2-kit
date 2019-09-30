package cc.whohow.vfs.version;

import cc.whohow.vfs.FileObject;

public class FileVersion<V> {
    private final FileObject fileObject;
    private final V version;

    public FileVersion(FileObject fileObject, V version) {
        this.fileObject = fileObject;
        this.version = version;
    }

    public FileObject getFileObject() {
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
