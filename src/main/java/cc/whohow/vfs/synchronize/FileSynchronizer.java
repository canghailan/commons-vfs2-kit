package cc.whohow.vfs.synchronize;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.version.FileLastModifiedTimeVersionProvider;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionProvider;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;

import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSynchronizer implements Runnable, Callable<Map<String, String>> {
    protected final FileObject source;
    protected final FileObject target;
    protected final FileVersionProvider<?> sourceVersionProvider;
    protected final FileVersionProvider<?> targetVersionProvider;
    protected final FileName sourceFileName;
    protected final FileName targetFileName;

    public FileSynchronizer(FileObject source,
                            FileObject target) {
        this(source, target, new FileLastModifiedTimeVersionProvider());
    }

    public FileSynchronizer(FileObject source,
                            FileObject target,
                            FileVersionProvider<?> fileVersionProvider) {
        this(source, target, fileVersionProvider, fileVersionProvider);
    }

    public FileSynchronizer(FileObject source,
                            FileObject target,
                            FileVersionProvider<?> sourceVersionProvider,
                            FileVersionProvider<?> targetVersionProvider) {
        this.source = source;
        this.target = target;
        this.sourceVersionProvider = sourceVersionProvider;
        this.targetVersionProvider = targetVersionProvider;
        this.sourceFileName = source.getName();
        this.targetFileName = target.getName();
    }

    @Override
    public synchronized void run() {
        try (Stream<? extends FileVersion<?>> sourceVersions = sourceVersionProvider.getVersions(source);
             Stream<? extends FileVersion<?>> targetVersions = targetVersionProvider.getVersions(target)) {
            Map<String, FileVersion<?>> index = sourceVersions
                    .collect(Collectors.toMap(self -> sourceKey(self.getFileObject().getName()), self -> self));
            Iterator<? extends FileVersion<?>> iterator = targetVersions.iterator();
            while (iterator.hasNext()) {
                FileVersion<?> targetVersion = iterator.next();
                String key = targetKey(targetVersion.getFileObject().getName());
                FileVersion<?> sourceVersion = index.remove(key);
                if (sourceVersion == null) {
                    delete(key, targetVersion.getFileObject());
                } else if (!Objects.equals(sourceVersion.getVersion(), targetVersion.getVersion())) {
                    change(key, sourceVersion.getFileObject(), targetVersion.getFileObject());
                }
            }
            for (Map.Entry<String, FileVersion<?>> e : index.entrySet()) {
                create(e.getKey(), e.getValue().getFileObject());
            }
        }
    }

    @Override
    public Map<String, String> call() {
        try (Stream<? extends FileVersion<?>> sourceVersions = sourceVersionProvider.getVersions(source);
             Stream<? extends FileVersion<?>> targetVersions = targetVersionProvider.getVersions(target)) {
            Map<String, FileVersion<?>> index = sourceVersions
                    .collect(Collectors.toMap(self -> sourceKey(self.getFileObject().getName()), self -> self));
            Iterator<? extends FileVersion<?>> iterator = targetVersions.iterator();

            Map<String, String> diff = new LinkedHashMap<>();
            while (iterator.hasNext()) {
                FileVersion<?> targetVersion = iterator.next();
                String key = targetKey(targetVersion.getFileObject().getName());
                FileVersion<?> sourceVersion = index.remove(key);
                if (sourceVersion == null) {
                    diff.put(key, "-");
                } else if (!Objects.equals(sourceVersion.getVersion(), targetVersion.getVersion())) {
                    diff.put(key, "*");
                } else {
                    diff.put(key, "=");
                }
            }
            for (Map.Entry<String, FileVersion<?>> e : index.entrySet()) {
                diff.put(e.getKey(), "+");
            }
            return diff;
        }
    }

    protected String sourceKey(FileName fileName) {
        try {
            return sourceFileName.getRelativeName(fileName);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String targetKey(FileName fileName) {
        try {
            return targetFileName.getRelativeName(fileName);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected FileObject targetFile(String key) {
        try {
            return target.resolveFile(key);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void create(String key, FileObject source) {
        try {
            targetFile(key).copyFrom(source, Selectors.SELECT_SELF);
            log("+", key);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void delete(String key, FileObject target) {
        try {
            target.deleteAll();
            log("-", key);
        } catch (FileNotFoundException ignore) {
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void change(String key, FileObject source, FileObject target) {
        try {
            target.copyFrom(source, Selectors.SELECT_SELF);
            log("*", key);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void log(String event, String key) {
    }
}
