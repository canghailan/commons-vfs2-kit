package cc.whohow.vfs.operations;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileOperation;

import java.util.Collections;
import java.util.Map;

public interface Move extends CloudFileOperation<Move.Options, Object> {
    class Options {
        private final CloudFileObject source;
        private final CloudFileObject destination;
        private final Map<String, Object> options;

        public Options(CloudFileObject source, CloudFileObject destination) {
            this(source, destination, Collections.emptyMap());
        }

        public Options(CloudFileObject source, CloudFileObject destination, Map<String, Object> options) {
            this.source = source;
            this.destination = destination;
            this.options = options;
        }

        public CloudFileObject getSource() {
            return source;
        }

        public CloudFileObject getDestination() {
            return destination;
        }

        public Map<String, Object> getOptions() {
            return options;
        }
    }
}
