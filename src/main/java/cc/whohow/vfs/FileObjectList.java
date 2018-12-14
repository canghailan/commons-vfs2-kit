package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;

import java.io.Closeable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface FileObjectList extends Iterable<FileObject>, Closeable {
    default Stream<FileObject> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
