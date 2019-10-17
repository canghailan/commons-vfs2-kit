package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileObject;

import java.nio.file.Watchable;

public interface FileWatchable extends Watchable {
    FileObject getFileObject();
}
