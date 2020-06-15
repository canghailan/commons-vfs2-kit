package cc.whohow.fs;

public interface FileWatchEvent<P extends Path, F extends File<P, F>> {
    Kind kind();

    File<P, F> watchable();

    File<P, F> file();

    enum Kind {
        CREATE("+"),
        DELETE("-"),
        MODIFY("*"),
        NOT_MODIFIED("=");

        private final String symbol;

        Kind(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
