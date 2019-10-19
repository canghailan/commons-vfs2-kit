package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.FileOperationX;

import java.util.Collections;
import java.util.Map;

public interface Copy extends FileOperationX<Copy.Options, Object> {
    class Options {
        private final FileObjectX source;
        private final FileObjectX destination;
        private final Map<String, Object> options;

        public Options(FileObjectX source, FileObjectX destination) {
            this(source, destination, Collections.emptyMap());
        }

        public Options(FileObjectX source, FileObjectX destination, Map<String, Object> options) {
            this.source = source;
            this.destination = destination;
            this.options = options;
        }

        public FileObjectX getSource() {
            return source;
        }

        public FileObjectX getDestination() {
            return destination;
        }

        public Map<String, Object> getOptions() {
            return options;
        }
    }
}
