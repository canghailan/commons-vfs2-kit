package cc.whohow.vfs.operations;

import cc.whohow.vfs.CloudFileObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class Unzip extends AbstractFileOperation<Unzip.Options, CloudFileObject> {
    @Override
    public CloudFileObject apply(Options options) {
        try (ZipInputStream stream = new ZipInputStream(options.getFile().getInputStream())) {
            return options.getDirectory();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class Options {
        private final CloudFileObject file;
        private final CloudFileObject directory;
        private final Map<String, Object> options;

        public Options(CloudFileObject file, CloudFileObject directory) {
            this(file, directory, Collections.emptyMap());
        }

        public Options(CloudFileObject file, CloudFileObject directory, Map<String, Object> options) {
            this.file = file;
            this.directory = directory;
            this.options = options;
        }

        public CloudFileObject getFile() {
            return file;
        }

        public CloudFileObject getDirectory() {
            return directory;
        }

        public Map<String, Object> getOptions() {
            return options;
        }
    }
}
