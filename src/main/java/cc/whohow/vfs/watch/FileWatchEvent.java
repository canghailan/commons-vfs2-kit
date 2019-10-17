package cc.whohow.vfs.watch;

import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;

import java.nio.file.WatchEvent;

public abstract class FileWatchEvent extends AbstractFileChangeEvent implements WatchEvent<CloudFileObject> {
    public FileWatchEvent(CloudFileObject file) {
        super(file);
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public CloudFileObject context() {
        return (CloudFileObject) getFile();
    }

    public static class Create extends FileWatchEvent {
        public Create(CloudFileObject context) {
            super(context);
        }

        @Override
        public Kind<CloudFileObject> kind() {
            return FileEventKind.CREATE;
        }

        @Override
        public void notify(FileListener listener) throws Exception {
            listener.fileCreated(this);
        }
    }

    public static class Delete extends FileWatchEvent {
        public Delete(CloudFileObject context) {
            super(context);
        }

        @Override
        public Kind<CloudFileObject> kind() {
            return FileEventKind.DELETE;
        }

        @Override
        public void notify(FileListener listener) throws Exception {
            listener.fileDeleted(this);
        }
    }

    public static class Modify extends FileWatchEvent {
        public Modify(CloudFileObject context) {
            super(context);
        }

        @Override
        public Kind<CloudFileObject> kind() {
            return FileEventKind.MODIFY;
        }

        @Override
        public void notify(FileListener listener) throws Exception {
            listener.fileChanged(this);
        }
    }
}
