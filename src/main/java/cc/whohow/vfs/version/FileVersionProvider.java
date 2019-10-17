package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileObjects;
import cc.whohow.vfs.io.UncheckedCloseable;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface FileVersionProvider<V> {
    FileVersion<V> getVersion(CloudFileObject fileObject);

    default Stream<FileVersion<V>> getVersions(CloudFileObject fileObject) {
        try {
            if (fileObject.isFolder()) {
                DirectoryStream<CloudFileObject> list = FileObjects.listRecursively(fileObject);
                return Stream.concat(
                        Stream.of(fileObject),
                        StreamSupport.stream(list.spliterator(), false))
                        .map(this::getVersion)
                        .filter(Objects::nonNull)
                        .onClose(new UncheckedCloseable(list));
            } else {
                return Stream.of(getVersion(fileObject));
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
