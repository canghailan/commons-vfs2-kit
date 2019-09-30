package cc.whohow.vfs.diff;

public class Diff {
    public static final char NO = '=';
    public static final char ADD = '+';
    public static final char DELETE = '-';
    public static final char UPDATE = '*';

    public static Diff no(String key) {
        return new Diff(NO, key);
    }

    public static Diff add(String key) {
        return new Diff(ADD, key);
    }

    public static Diff delete(String key) {
        return new Diff(DELETE, key);
    }

    public static Diff update(String key) {
        return new Diff(UPDATE, key);
    }

    private final char type;
    private final String key;

    public Diff(char type, String key) {
        this.type = type;
        this.key = key;
    }

    public char getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return type + " " + key;
    }
}
