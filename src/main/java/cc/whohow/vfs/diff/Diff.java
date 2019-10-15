package cc.whohow.vfs.diff;

public class Diff<K> {
    public static final char NO = '=';
    public static final char ADD = '+';
    public static final char DELETE = '-';
    public static final char UPDATE = '*';

    public static <K> Diff<K> no(K key) {
        return new Diff<>(NO, key);
    }

    public static <K> Diff<K> add(K key) {
        return new Diff<>(ADD, key);
    }

    public static <K> Diff<K> delete(K key) {
        return new Diff<>(DELETE, key);
    }

    public static <K> Diff<K> update(K key) {
        return new Diff<>(UPDATE, key);
    }

    private final char type;
    private final K key;

    public Diff(char type, K key) {
        this.type = type;
        this.key = key;
    }

    public char getType() {
        return type;
    }

    public K getKey() {
        return key;
    }

    @Override
    public String toString() {
        return type + " " + key;
    }

    public static Diff<String> parse(String text) {
        return new Diff<>(text.charAt(0), text.substring(2));
    }
}
