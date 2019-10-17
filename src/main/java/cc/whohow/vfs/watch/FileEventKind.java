package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileObject;

import java.nio.file.WatchEvent;

public enum FileEventKind implements WatchEvent.Kind<FileObject> {
    CREATE('+'),
    DELETE('-'),
    MODIFY('*'),
    NOT_MODIFIED('=');

    private final char symbol;

    FileEventKind(char symbol) {
        this.symbol = symbol;
    }

    public static FileEventKind of(char symbol) {
        switch (symbol) {
            case '+':
                return CREATE;
            case '-':
                return DELETE;
            case '*':
                return MODIFY;
            case '=':
                return NOT_MODIFIED;
            default:
                throw new IllegalArgumentException(String.valueOf(symbol));
        }
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isNotModified() {
        return this == NOT_MODIFIED;
    }

    @Override
    public Class<FileObject> type() {
        return FileObject.class;
    }
}
