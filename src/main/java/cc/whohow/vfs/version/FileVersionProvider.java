package cc.whohow.vfs.version;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileObjectList;
import cc.whohow.vfs.FileObjects;

import java.util.Objects;
import java.util.stream.Stream;

public interface FileVersionProvider<V> {
    FileVersion<V> getVersion(FileObject fileObject);

    default Stream<FileVersion<V>> getVersions(FileObject fileObject) {
        FileObjectList list = FileObjects.listRecursively(fileObject);
        return Stream.concat(
                Stream.of(fileObject),
                list.stream())
                .map(this::getVersion)
                .filter(Objects::nonNull)
                .onClose(list::close);
    }
}
