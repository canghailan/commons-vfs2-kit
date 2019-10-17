package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;

import java.nio.file.WatchEvent;

public abstract class FileWatchEvent extends AbstractFileChangeEvent implements WatchEvent<FileObject> {
    public FileWatchEvent(FileObject file) {
        super(file);
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public FileObject context() {
        return getFile();
    }

    public static class Create extends FileWatchEvent {
        public Create(FileObject context) {
            super(context);
        }

        @Override
        public Kind<FileObject> kind() {
            return FileEventKind.CREATE;
        }

        @Override
        public void notify(FileListener listener) throws Exception {
            listener.fileCreated(this);
        }
    }

    public static class Delete extends FileWatchEvent {
        public Delete(FileObject context) {
            super(context);
        }

        @Override
        public Kind<FileObject> kind() {
            return FileEventKind.DELETE;
        }

        @Override
        public void notify(FileListener listener) throws Exception {
            listener.fileDeleted(this);
        }
    }

    public static class Modify extends FileWatchEvent {
        public Modify(FileObject context) {
            super(context);
        }

        @Override
        public Kind<FileObject> kind() {
            return FileEventKind.MODIFY;
        }

        @Override
        public void notify(FileListener listener) throws Exception {
            listener.fileChanged(this);
        }
    }
}
