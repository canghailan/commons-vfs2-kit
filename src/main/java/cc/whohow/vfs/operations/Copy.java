package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileOperation;

import java.util.Collections;
import java.util.Map;

public interface Copy extends FileOperation<Copy.Options, Object> {
    class Options {
        private final FileObject source;
        private final FileObject destination;
        private final Map<String, Object> options;

        public Options(FileObject source, FileObject destination) {
            this(source, destination, Collections.emptyMap());
        }

        public Options(FileObject source, FileObject destination, Map<String, Object> options) {
            this.source = source;
            this.destination = destination;
            this.options = options;
        }

        public FileObject getSource() {
            return source;
        }

        public FileObject getDestination() {
            return destination;
        }

        public Map<String, Object> getOptions() {
            return options;
        }
    }
}
