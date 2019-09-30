package cc.whohow.vfs;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface FileObjectList extends Iterable<FileObject>, Closeable {
    default List<FileObject> toList() {
        return stream().collect(Collectors.toList());
    }

    default Stream<FileObject> stream() {
        return StreamSupport.stream(spliterator(), false).onClose(this::close);
    }

    @Override
    void close();
}
