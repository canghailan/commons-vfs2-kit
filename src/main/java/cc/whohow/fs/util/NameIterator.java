package cc.whohow.fs.util;

import java.util.ListIterator;

/**
 * 子路径遍历器，按分隔符切分路径，不支持空路径
 */
public class NameIterator implements ListIterator<CharSequence> {
    private final CharSequence names;
    private final char separatorChar;
    private final int beforeFirstIndex;
    private final int afterLastIndex;
    private int index;

    public NameIterator(CharSequence names, char separatorChar) {
        if (names == null || names.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.names = names;
        this.separatorChar = separatorChar;
        this.beforeFirstIndex = startsWithSeparator() ? 0 : -1;
        this.afterLastIndex = endsWithSeparator() ? names.length() - 1 : names.length();
        this.index = beforeFirstIndex;
    }

    public CharSequence getNames() {
        return names;
    }

    public char getSeparatorChar() {
        return separatorChar;
    }

    public boolean startsWithSeparator() {
        return names.charAt(0) == separatorChar;
    }

    public boolean endsWithSeparator() {
        return names.charAt(names.length() - 1) == separatorChar;
    }

    public boolean isBeforeFirst() {
        return index == beforeFirstIndex;
    }

    public boolean isAfterLast() {
        return index == afterLastIndex;
    }

    public NameIterator beforeFirst() {
        index = beforeFirstIndex;
        return this;
    }

    public NameIterator afterLast() {
        index = afterLastIndex;
        return this;
    }

    public NameIterator absolute(int n) {
        return beforeFirst().relative(n);
    }

    public NameIterator relative(int n) {
        if (n > 0) {
            while (n-- > 0) {
                if (hasNext()) {
                    skipNext();
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
        } else if (n < 0) {
            while (n++ < 0) {
                if (hasPrevious()) {
                    skipPrevious();
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        return index < afterLastIndex;
    }

    public void skipNext() {
        index = nextSeparatorIndex();
    }

    @Override
    public CharSequence next() {
        int begin = index + 1;
        index = nextSeparatorIndex();
        return names.subSequence(begin, index);
    }

    @Override
    public boolean hasPrevious() {
        return index > beforeFirstIndex;
    }

    public void skipPrevious() {
        index = previousSeparatorIndex();
    }

    @Override
    public CharSequence previous() {
        int end = index;
        index = previousSeparatorIndex();
        return names.subSequence(index + 1, end);
    }

    public int currentIndex() {
        int current = index;
        int currentIndex = 0;
        while (hasPrevious()) {
            currentIndex++;
            skipPrevious();
        }
        index = current;
        return currentIndex;
    }

    @Override
    public int nextIndex() {
        return currentIndex() + 1;
    }

    @Override
    public int previousIndex() {
        return currentIndex() - 1;
    }

    public int currentSeparatorIndex() {
        return index;
    }

    public int nextSeparatorIndex() {
        for (int i = index + 1; i < afterLastIndex; i++) {
            if (names.charAt(i) == separatorChar) {
                return i;
            }
        }
        return afterLastIndex;
    }

    public int previousSeparatorIndex() {
        for (int i = index - 1; i > beforeFirstIndex; i--) {
            if (names.charAt(i) == separatorChar) {
                return i;
            }
        }
        return beforeFirstIndex;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(CharSequence s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(CharSequence s) {
        throw new UnsupportedOperationException();
    }
}