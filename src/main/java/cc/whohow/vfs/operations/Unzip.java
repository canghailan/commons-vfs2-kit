package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileObjectX;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class Unzip extends AbstractFileOperation<Unzip.Options, FileObjectX> {
    @Override
    public FileObjectX apply(Options options) {
        try (ZipInputStream stream = new ZipInputStream(options.getFile().getInputStream())) {
            return options.getDirectory();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class Options {
        private final FileObjectX file;
        private final FileObjectX directory;
        private final Map<String, Object> options;

        public Options(FileObjectX file, FileObjectX directory) {
            this(file, directory, Collections.emptyMap());
        }

        public Options(FileObjectX file, FileObjectX directory, Map<String, Object> options) {
            this.file = file;
            this.directory = directory;
            this.options = options;
        }

        public FileObjectX getFile() {
            return file;
        }

        public FileObjectX getDirectory() {
            return directory;
        }

        public Map<String, Object> getOptions() {
            return options;
        }
    }
}
