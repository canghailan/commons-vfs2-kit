package cc.whohow.vfs;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface CloudFileObjectList extends Iterable<CloudFileObject>, Closeable {
    default List<CloudFileObject> toList() {
        return stream().collect(Collectors.toList());
    }

    default Stream<CloudFileObject> stream() {
        return StreamSupport.stream(spliterator(), false).onClose(this::close);
    }

    @Override
    void close();
}
