package cc.whohow.vfs;

import cc.whohow.fs.FileWatchEvent;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

import java.util.Objects;
import java.util.function.Consumer;

public class FileListenerAdapter<E extends FileWatchEvent<?, ?>> implements Consumer<E> {
    protected final FileSystemAdapter fileSystem;
    protected final FileListener fileListener;

    public FileListenerAdapter(FileSystemAdapter fileSystem, FileListener fileListener) {
        this.fileSystem = fileSystem;
        this.fileListener = fileListener;
    }

    @Override
    public void accept(E fileWatchEvent) {
        try {
            switch (fileWatchEvent.kind()) {
                case CREATE: {
                    fileListener.fileCreated(new FileChangeEvent(
                            new FileObjectAdapter(new FilePath(fileSystem, fileWatchEvent.file()))));
                }
                case DELETE: {
                    fileListener.fileDeleted(new FileChangeEvent(
                            new FileObjectAdapter(new FilePath(fileSystem, fileWatchEvent.file()))));
                }
                case MODIFY: {
                    fileListener.fileChanged(new FileChangeEvent(
                            new FileObjectAdapter(new FilePath(fileSystem, fileWatchEvent.file()))));
                }
                default: {
                    break;
                }
            }
        } catch (Exception e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FileListenerAdapter) {
            FileListenerAdapter<?> that = (FileListenerAdapter<?>) o;
            return fileSystem.equals(that.fileSystem) &&
                    fileListener.equals(that.fileListener);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, fileListener);
    }
}
