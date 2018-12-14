package cc.whohow.vfs.diff;

public enum DiffStatus {
    NOT_MODIFIED("="),
    MODIFIED("*"),
    ADDED("+"),
    DELETED("-");

    private final String string;

    DiffStatus(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}