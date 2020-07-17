package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.FileAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

public class MapFileComparator implements Iterator<FileDiff>, Closeable {
    private static final Logger log = LogManager.getLogger(MapFileComparator.class);
    protected final LongAdder createCount = new LongAdder();
    protected final LongAdder deleteCount = new LongAdder();
    protected final LongAdder modifyCount = new LongAdder();
    protected final LongAdder notModifiedCount = new LongAdder();
    protected boolean deleteStage = false;
    protected Map<String, FileAttributes> index;
    protected Comparator<FileAttributes> comparator;
    protected Iterator<FileAttributes> source;
    protected String sourcePathKey;
    protected Iterator<FileAttributes> target;
    protected String targetPathKey;
    protected Iterator<String> deleteFiles;
    protected Runnable onClose;

    private MapFileComparator() {
    }

    @Override
    public boolean hasNext() {
        if (deleteStage) {
            return deleteFiles.hasNext();
        } else {
            if (source.hasNext()) {
                return true;
            } else {
                deleteStage = true;
                deleteFiles = index.keySet().iterator();
                return deleteFiles.hasNext();
            }
        }
    }

    @Override
    public FileDiff next() {
        if (deleteStage) {
            String pathKey = deleteFiles.next();
            deleteCount.increment();
            log.trace("{} {}", FileDiff.DELETE, pathKey);
            return FileDiff.delete(pathKey);
        } else {
            FileAttributes sourceFileAttributes = source.next();
            String pathKey = sourceFileAttributes.getAsString(sourcePathKey)
                    .orElseThrow(IllegalStateException::new);
            FileAttributes targetFileAttributes = index.remove(pathKey);

            if (targetFileAttributes == null) {
                createCount.increment();
                log.trace("{} {}", FileDiff.CREATE, pathKey);
                return FileDiff.create(pathKey);
            } else {
                if (comparator.compare(sourceFileAttributes, targetFileAttributes) > 0) {
                    modifyCount.increment();
                    log.trace("{} {}", FileDiff.MODIFY, pathKey);
                    return FileDiff.modify(pathKey);
                } else {
                    notModifiedCount.increment();
                    return FileDiff.notModified(pathKey);
                }
            }
        }
    }

    public long getSourceCount() {
        return getCreateCount() + getModifyCount() + getNotModifiedCount();
    }

    public long getTargetCount() {
        return getDeleteCount() + getModifyCount() + getNotModifiedCount();
    }

    public long getCreateCount() {
        return createCount.longValue();
    }

    public long getDeleteCount() {
        return deleteCount.longValue();
    }

    public long getModifyCount() {
        return modifyCount.longValue();
    }

    public long getNotModifiedCount() {
        return notModifiedCount.longValue();
    }

    @Override
    public void close() throws IOException {
        if (onClose != null) {
            onClose.run();
        }
    }

    public static class Builder {
        protected Map<String, FileAttributes> index;
        protected Comparator<FileAttributes> comparator;
        protected Iterator<FileAttributes> source;
        protected String sourcePathKey = "PK";
        protected Iterator<FileAttributes> target;
        protected String targetPathKey = "PK";
        protected Runnable onClose;

        public Builder index(Map<String, FileAttributes> index) {
            this.index = index;
            return this;
        }

        public Builder comparator(Comparator<FileAttributes> comparator) {
            this.comparator = comparator;
            return this;
        }

        public Builder source(Iterator<FileAttributes> source) {
            this.source = source;
            return this;
        }

        public void sourcePathKey(String sourcePathKey) {
            this.sourcePathKey = sourcePathKey;
        }

        public Builder target(Iterator<FileAttributes> target) {
            this.target = target;
            return this;
        }

        public void targetPathKey(String targetPathKey) {
            this.targetPathKey = targetPathKey;
        }

        public Builder onClose(Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        public MapFileComparator build() {
            Objects.requireNonNull(index);
            Objects.requireNonNull(comparator);
            Objects.requireNonNull(source);
            Objects.requireNonNull(target);

            while (target.hasNext()) {
                FileAttributes fileAttributes = target.next();
                String pathKey = fileAttributes.getAsString(targetPathKey)
                        .orElseThrow(IllegalStateException::new);
                index.put(pathKey, fileAttributes);
            }

            MapFileComparator mapFileComparator = new MapFileComparator();
            mapFileComparator.index = index;
            mapFileComparator.comparator = comparator;
            mapFileComparator.source = source;
            mapFileComparator.sourcePathKey = sourcePathKey;
            mapFileComparator.target = target;
            mapFileComparator.targetPathKey = targetPathKey;
            mapFileComparator.onClose = onClose;
            return mapFileComparator;
        }
    }
}
