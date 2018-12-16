package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.NameScope;

public class FileWatchListener implements FileListener {
    protected final FileName fileName;
    protected final FileListener fileListener;

    private FileWatchListener(FileName fileName, FileListener fileListener) {
        this.fileName = fileName;
        this.fileListener = fileListener;
    }

    public static FileWatchListener create(FileName watch, FileName fileName, FileListener fileListener) {
        if (watch.equals(fileName)) {
            return new FileWatchListener(fileName, fileListener);
        } else {
            return new Filtered(fileName, fileListener);
        }
    }

    public static FileWatchListener create(FileName watch, FileListener fileListener) {
        return create(watch, watch, fileListener);
    }

    public static FileWatchListener create(FileName watch, FileWatchListener fileListener) {
        return create(watch, fileListener.fileName, fileListener.fileListener);
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        fileListener.fileCreated(event);
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        fileListener.fileDeleted(event);
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        fileListener.fileChanged(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FileWatchListener) {
            FileWatchListener that = (FileWatchListener) o;
            return fileName.equals(that.fileName) &&
                    fileListener.equals(that.fileListener);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileName.hashCode() * 31 + fileListener.hashCode();
    }

    @Override
    public String toString() {
        return fileListener.toString();
    }

    public static class Filtered extends FileWatchListener {
        public Filtered(FileName fileName, FileListener fileListener) {
            super(fileName, fileListener);
        }

        public boolean accept(FileChangeEvent event) {
            return fileName.isDescendent(event.getFile().getName(), NameScope.DESCENDENT_OR_SELF);
        }

        @Override
        public void fileCreated(FileChangeEvent event) throws Exception {
            if (accept(event)) {
                fileListener.fileCreated(event);
            }
        }

        @Override
        public void fileDeleted(FileChangeEvent event) throws Exception {
            if (accept(event)) {
                fileListener.fileDeleted(event);
            }
        }

        @Override
        public void fileChanged(FileChangeEvent event) throws Exception {
            if (accept(event)) {
                fileListener.fileChanged(event);
            }
        }
    }
}
