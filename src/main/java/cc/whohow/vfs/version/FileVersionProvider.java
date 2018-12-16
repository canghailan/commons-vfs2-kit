package cc.whohow.vfs.version;

import org.apache.commons.vfs2.FileObject;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface FileVersionProvider<V> {
    FileVersion<V> getVersion(FileObject fileObject);

    default Stream<FileVersion<V>> getVersions(FileObject fileObject) {
        return Stream.concat(
                Stream.of(fileObject),
                StreamSupport.stream(fileObject.spliterator(), false))
                .map(this::getVersion)
                .filter(Objects::nonNull);
    }
}
