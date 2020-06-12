package cc.whohow.fs;

public interface FileWatchEvent<P extends Path, F extends File<P, F>> {
    Kind kind();

    File<P, F> watchable();

    File<P, F> file();

    class Kind {
        public static final Kind CREATE = new Kind("+");
        public static final Kind DELETE = new Kind("-");
        public static final Kind MODIFY = new Kind("*");
        public static final Kind NOT_MODIFIED = new Kind("=");

        private final String name;

        public Kind(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
