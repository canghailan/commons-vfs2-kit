package cc.whohow.vfs;

import cc.whohow.fs.FileEvent;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;

import java.util.function.Consumer;

public class FileListenerAdapter implements Consumer<FileEvent> {
    protected final VirtualFileSystemAdapter vfs;
    protected final FileListener fileListener;

    public FileListenerAdapter(VirtualFileSystemAdapter vfs, FileListener fileListener) {
        this.vfs = vfs;
        this.fileListener = fileListener;
    }

    @Override
    public void accept(FileEvent fileEvent) {
        try {
            FileObject fileObject = new FileObjectAdapter(vfs, fileEvent.file());
            switch (fileEvent.kind()) {
                case CREATE: {
                    fileListener.fileCreated(new FileChangeEvent(fileObject));
                    break;
                }
                case DELETE: {
                    fileListener.fileDeleted(new FileChangeEvent(fileObject));
                    break;
                }
                case MODIFY: {
                    fileListener.fileChanged(new FileChangeEvent(fileObject));
                    break;
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
            FileListenerAdapter that = (FileListenerAdapter) o;
            return fileListener.equals(that.fileListener);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileListener.hashCode();
    }

    @Override
    public String toString() {
        return fileListener.toString();
    }
}
