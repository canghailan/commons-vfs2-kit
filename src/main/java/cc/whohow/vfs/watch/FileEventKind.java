package cc.whohow.vfs.watch;

import cc.whohow.vfs.CloudFileObject;

import java.nio.file.WatchEvent;

public enum FileEventKind implements WatchEvent.Kind<CloudFileObject> {
    CREATE('+'),
    DELETE('-'),
    MODIFY('*'),
    NOT_MODIFIED('=');

    private final char symbol;

    FileEventKind(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isNotModified() {
        return this == NOT_MODIFIED;
    }

    @Override
    public Class<CloudFileObject> type() {
        return CloudFileObject.class;
    }

    public static FileEventKind of(char symbol) {
        switch (symbol) {
            case '+': return CREATE;
            case '-': return DELETE;
            case '*': return MODIFY;
            case '=': return NOT_MODIFIED;
            default: throw new IllegalArgumentException(String.valueOf(symbol));
        }
    }
}
