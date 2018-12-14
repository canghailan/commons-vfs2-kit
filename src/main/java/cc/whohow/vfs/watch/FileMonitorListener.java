package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;

public class FileMonitorListener implements FileListener {
    protected final FileName fileName;
    protected final FileListener fileListener;

    private FileMonitorListener(FileName fileName, FileListener fileListener) {
        this.fileName = fileName;
        this.fileListener = fileListener;
    }

    public static FileMonitorListener create(FileName watch, FileName fileName, FileListener fileListener) {
        if (watch.equals(fileName)) {
            return new FileMonitorListener(fileName, fileListener);
        } else {
            return new Filtered(fileName, fileListener);
        }
    }

    public static FileMonitorListener create(FileName watch, FileMonitorListener fileListener) {
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
        if (o instanceof FileMonitorListener) {
            FileMonitorListener that = (FileMonitorListener) o;
            return fileName.equals(that.fileName) &&
                    fileListener.equals(that.fileListener);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileName.hashCode() * 31 + fileListener.hashCode();
    }

    public static class Filtered extends FileMonitorListener {
        public Filtered(FileName fileName, FileListener fileListener) {
            super(fileName, fileListener);
        }

        @Override
        public void fileCreated(FileChangeEvent event) throws Exception {
            if (event.getFile().getName().isAncestor(fileName)) {
                fileListener.fileCreated(event);
            }
        }

        @Override
        public void fileDeleted(FileChangeEvent event) throws Exception {
            if (event.getFile().getName().isAncestor(fileName)) {
                fileListener.fileDeleted(event);
            }
        }

        @Override
        public void fileChanged(FileChangeEvent event) throws Exception {
            if (event.getFile().getName().isAncestor(fileName)) {
                fileListener.fileChanged(event);
            }
        }
    }
}
