package cc.whohow.vfs.watch;

import java.util.Map;

public abstract class FileDiffEntry<K> implements Map.Entry<K, FileEventKind> {
    private final K key;

    public FileDiffEntry(K key) {
        this.key = key;
    }

    public static <K> FileDiffEntry<K> create(FileEventKind kind, K key) {
        switch (kind) {
            case CREATE:
                return new Create<>(key);
            case DELETE:
                return new Delete<>(key);
            case MODIFY:
                return new Modify<>(key);
            case NOT_MODIFIED:
                return new NotModified<>(key);
            default:
                throw new IllegalArgumentException(kind.toString());
        }
    }

    public static FileDiffEntry<String> parse(String text) {
        return create(FileEventKind.of(text.charAt(0)), text.substring(2));
    }

    @Override
    public FileEventKind setValue(FileEventKind value) {
        throw new UnsupportedOperationException();
    }

    public K getKey() {
        return key;
    }

    public boolean isModified() {
        return getValue() != FileEventKind.NOT_MODIFIED;
    }

    public boolean isNotModified() {
        return getValue() == FileEventKind.NOT_MODIFIED;
    }

    @Override
    public String toString() {
        return getValue().getSymbol() + " " + key;
    }

    public static class Create<K> extends FileDiffEntry<K> {
        public Create(K key) {
            super(key);
        }

        @Override
        public FileEventKind getValue() {
            return FileEventKind.CREATE;
        }
    }

    public static class Delete<K> extends FileDiffEntry<K> {
        public Delete(K key) {
            super(key);
        }

        @Override
        public FileEventKind getValue() {
            return FileEventKind.DELETE;
        }
    }

    public static class Modify<K> extends FileDiffEntry<K> {
        public Modify(K key) {
            super(key);
        }

        @Override
        public FileEventKind getValue() {
            return FileEventKind.MODIFY;
        }
    }

    public static class NotModified<K> extends FileDiffEntry<K> {
        public NotModified(K key) {
            super(key);
        }

        @Override
        public FileEventKind getValue() {
            return FileEventKind.NOT_MODIFIED;
        }
    }
}
