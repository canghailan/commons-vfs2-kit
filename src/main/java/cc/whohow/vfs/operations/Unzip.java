package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class Unzip extends AbstractFileOperation<Unzip.Options, FileObject> {
    @Override
    public FileObject apply(Options options) {
        try (ZipInputStream stream = new ZipInputStream(options.getFile().getInputStream())) {
            return options.getDirectory();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class Options {
        private final FileObject file;
        private final FileObject directory;
        private final Map<String, Object> options;

        public Options(FileObject file, FileObject directory) {
            this(file, directory, Collections.emptyMap());
        }

        public Options(FileObject file, FileObject directory, Map<String, Object> options) {
            this.file = file;
            this.directory = directory;
            this.options = options;
        }

        public FileObject getFile() {
            return file;
        }

        public FileObject getDirectory() {
            return directory;
        }

        public Map<String, Object> getOptions() {
            return options;
        }
    }
}
