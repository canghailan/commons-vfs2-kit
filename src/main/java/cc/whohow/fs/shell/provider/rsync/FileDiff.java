package cc.whohow.fs.shell.provider.rsync;

import java.util.Objects;

public class FileDiff {
    public static final String CREATE = "+";
    public static final String DELETE = "-";
    public static final String MODIFY = "*";
    public static final String NOT_MODIFIED = "=";

    private final String kind;
    private final String file;

    public FileDiff(String text) {
        if (text.charAt(1) != ' ') {
            throw new IllegalArgumentException(text);
        }
        if (text.charAt(0) == '+'
                || text.charAt(0) == '-'
                || text.charAt(0) == '*'
                || text.charAt(0) == '=') {
            this.kind = text.substring(0, 1);
        } else {
            throw new IllegalArgumentException(text);
        }
        this.file = text.substring(2);
    }

    public FileDiff(String kind, String file) {
        this.kind = kind;
        this.file = file;
    }

    public static FileDiff create(String file) {
        return new FileDiff(CREATE, file);
    }

    public static FileDiff delete(String file) {
        return new FileDiff(DELETE, file);
    }

    public static FileDiff modify(String file) {
        return new FileDiff(MODIFY, file);
    }

    public static FileDiff notModified(String file) {
        return new FileDiff(NOT_MODIFIED, file);
    }

    public String getKind() {
        return kind;
    }

    public String getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FileDiff) {
            FileDiff that = (FileDiff) o;
            return kind.equals(that.kind) &&
                    file.equals(that.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, file);
    }

    @Override
    public String toString() {
        return kind + " " + file;
    }
}
