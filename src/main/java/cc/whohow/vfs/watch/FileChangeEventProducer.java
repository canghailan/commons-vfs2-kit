package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;
import org.apache.commons.vfs2.events.ChangedEvent;
import org.apache.commons.vfs2.events.CreateEvent;
import org.apache.commons.vfs2.events.DeleteEvent;

import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.Callable;

public class FileChangeEventProducer implements Callable<List<? extends AbstractFileChangeEvent>> {
    private final FileObject fileObject;
    private final String watchAttributeName;
    private volatile Map<FileName, FileObjectSnapshot> snapshots;

    public FileChangeEventProducer(FileObject fileObject) {
        this(fileObject, "eTag");
    }

    public FileChangeEventProducer(FileObject fileObject, String watchAttributeName) {
        this.fileObject = fileObject;
        this.watchAttributeName = watchAttributeName;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public String getWatchAttributeName() {
        return watchAttributeName;
    }

    @Override
    public synchronized List<? extends AbstractFileChangeEvent> call() {
        try {
            Map<FileName, FileObjectSnapshot> oldSnapshots = snapshots;
            Map<FileName, FileObjectSnapshot> newSnapshots = getFileObjectSnapshots();
            snapshots = newSnapshots;

            if (oldSnapshots == null) {
                return Collections.emptyList();
            }

            List<AbstractFileChangeEvent> list = new ArrayList<>();
            for (Map.Entry<FileName, FileObjectSnapshot> e : newSnapshots.entrySet()) {
                FileObjectSnapshot newSnapshot = e.getValue();
                FileObjectSnapshot oldSnapshot = oldSnapshots.remove(e.getKey());
                if (oldSnapshot == null) {
                    list.add(new CreateEvent(newSnapshot.fileObject));
                } else if (!Objects.equals(oldSnapshot.watchAttribute, newSnapshot.watchAttribute)) {
                    list.add(new ChangedEvent(newSnapshot.fileObject));
                }
            }
            for (FileObjectSnapshot oldSnapshot : oldSnapshots.values()) {
                list.add(new DeleteEvent(oldSnapshot.fileObject));
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<FileName, FileObjectSnapshot> getFileObjectSnapshots() {
        Map<FileName, FileObjectSnapshot> snapshots = new HashMap<>();
        try {
            for (FileObject fileObject : fileObject) {
                if (fileObject.isFile()) {
                    try (FileContent fileContent = fileObject.getContent()) {
                        Object attribute = fileContent.getAttribute(watchAttributeName);
                        snapshots.put(fileObject.getName(), new FileObjectSnapshot(fileObject, attribute));
                    }
                }
            }
            return snapshots;
        } catch (FileNotFoundException e) {
            return snapshots;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    static class FileObjectSnapshot {
        final FileObject fileObject;
        final Object watchAttribute;

        FileObjectSnapshot(FileObject fileObject, Object watchAttribute) {
            this.fileObject = fileObject;
            this.watchAttribute = watchAttribute;
        }
    }
}
