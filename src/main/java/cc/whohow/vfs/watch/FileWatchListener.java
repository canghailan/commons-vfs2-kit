package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;

import java.util.function.Predicate;

public class FileWatchListener implements FileListener, Predicate<FileChangeEvent> {
    protected final FileName fileName;
    protected final FileListener fileListener;

    public FileWatchListener(FileName fileName, FileListener fileListener) {
        this.fileName = fileName;
        this.fileListener = fileListener;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileListener getFileListener() {
        return fileListener;
    }

    @Override
    public boolean test(FileChangeEvent event) {
        FileName eventFileName = event.getFile().getName();
        return fileName.equals(eventFileName) ||
                fileName.isDescendent(eventFileName);
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        if (test(event)) {
            fileListener.fileCreated(event);
        }
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        if (test(event)) {
            fileListener.fileDeleted(event);
        }
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        if (test(event)) {
            fileListener.fileChanged(event);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FileWatchListener) {
            FileWatchListener that = (FileWatchListener) o;
            return fileListener.equals(that.fileListener) &&
                    fileName.equals(that.fileName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileListener.hashCode() * 31 + fileName.hashCode();
    }

    @Override
    public String toString() {
        return fileListener.toString();
    }
}
