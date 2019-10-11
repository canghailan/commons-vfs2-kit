package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileObjectList;
import cc.whohow.vfs.FileObjects;

import java.util.Objects;
import java.util.stream.Stream;

public interface FileVersionProvider<V> {
    FileVersion<V> getVersion(CloudFileObject fileObject);

    default Stream<FileVersion<V>> getVersions(CloudFileObject fileObject) {
        CloudFileObjectList list = FileObjects.listRecursively(fileObject);
        return Stream.concat(
                Stream.of(fileObject),
                list.stream())
                .map(this::getVersion)
                .filter(Objects::nonNull)
                .onClose(list::close);
    }
}
