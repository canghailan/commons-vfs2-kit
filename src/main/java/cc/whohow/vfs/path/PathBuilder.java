package cc.whohow.vfs.path;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.stream.Stream;

/**
 * 路径构建器
 */
public class PathBuilder implements Iterable<CharSequence> {
    // 路径分隔符
    private char separatorChar;
    // 是否以路径分隔符开始
    private boolean startsWithSeparator = false;
    // 是否以路径分隔符结尾
    private boolean endsWithSeparator = false;
    // 子路径列表
    private LinkedList<CharSequence> names = new LinkedList<>();

    public PathBuilder() {
        this("");
    }

    public PathBuilder(CharSequence names) {
        this(names, '/');
    }

    public PathBuilder(CharSequence names, char separatorChar) {
        this.separatorChar = separatorChar;
        if (names != null && names.length() > 0) {
            NameIterator iterator = new NameIterator(names, separatorChar);
            this.startsWithSeparator = iterator.startsWithSeparator();
            this.endsWithSeparator = iterator.endsWithSeparator();
            while (iterator.hasNext()) {
                this.names.addLast(iterator.next());
            }
        }
    }

    public static boolean contentEquals(CharSequence a, CharSequence b) {
        if (a.length() != b.length()) {
            return false;
        }
        int length = a.length();
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算两个URI的相对路径
     */
    public static String relativize(URI ancestor, URI descendant) {
        return ancestor.relativize(descendant).normalize().getPath();
    }

    /**
     * 计算两个URI的相对路径
     */
    public static String relativize(String ancestor, String descendant) {
        return relativize(URI.create(ancestor), URI.create(descendant));
    }

    public char separatorChar() {
        return separatorChar;
    }

    public boolean startsWithSeparator() {
        return startsWithSeparator;
    }

    public boolean endsWithSeparator() {
        return endsWithSeparator;
    }

    public PathBuilder separatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
        return this;
    }

    public PathBuilder startsWithSeparator(boolean startWithSeparator) {
        this.startsWithSeparator = startWithSeparator;
        return this;
    }

    public PathBuilder endsWithSeparator(boolean endsWithSeparator) {
        this.endsWithSeparator = endsWithSeparator;
        return this;
    }

    /**
     * 相对当前路径，解析路径（包括相对路径、绝对路径）
     */
    public PathBuilder resolve(CharSequence path) {
        return resolve(path, separatorChar);
    }

    /**
     * 相对当前路径，解析路径（包括相对路径、绝对路径）
     */
    public PathBuilder resolve(CharSequence path, char separatorChar) {
        if (path == null || path.length() == 0) {
            return this;
        }

        NameIterator iterator = new NameIterator(path, separatorChar);
        if (iterator.startsWithSeparator()) {
            startsWithSeparator = iterator.startsWithSeparator();
            endsWithSeparator = iterator.endsWithSeparator();
            names.clear();
        } else {
            endsWithSeparator = iterator.endsWithSeparator();
        }
        while (iterator.hasNext()) {
            names.addLast(iterator.next());
        }
        return this;
    }

    public PathBuilder addFirst(CharSequence name) {
        this.names.addFirst(name);
        return this;
    }

    public PathBuilder addFirst(Collection<? extends CharSequence> names) {
        this.names.addAll(0, names);
        return this;
    }

    public PathBuilder addLast(CharSequence name) {
        this.names.addLast(name);
        return this;
    }

    public PathBuilder addLast(Iterator<? extends CharSequence> names) {
        while (names.hasNext()) {
            this.names.addLast(names.next());
        }
        return this;
    }

    public PathBuilder addLast(Iterable<? extends CharSequence> names) {
        return addLast(names.iterator());
    }

    public PathBuilder addLast(Collection<? extends CharSequence> names) {
        this.names.addAll(names);
        return this;
    }

    public PathBuilder removeFirst() {
        this.names.removeFirst();
        return this;
    }

    public PathBuilder removeLast() {
        this.names.removeLast();
        return this;
    }

    /**
     * 转为相对路径
     */
    public PathBuilder relativize(CharSequence path) {
        return relativize(path, separatorChar);
    }

    /**
     * 转为相对路径
     */
    public PathBuilder relativize(CharSequence path, char separatorChar) {
        return relativize(new PathBuilder(path, separatorChar));
    }

    /**
     * 转为相对路径
     */
    public PathBuilder relativize(PathBuilder that) {
        if (that.startsWithSeparator() != startsWithSeparator()) {
            throw new IllegalArgumentException();
        }
        startsWithSeparator = false;
        endsWithSeparator = that.endsWithSeparator();

        // 首先尽量规范化
        this.tryNormalize();
        that.tryNormalize();

        LinkedList<CharSequence> relative = new LinkedList<>();
        Iterator<CharSequence> thisIterator = this.iterator();
        Iterator<CharSequence> thatIterator = that.iterator();
        // 去除公共前缀
        while (thisIterator.hasNext()) {
            CharSequence thisNext = thisIterator.next();
            if (thatIterator.hasNext()) {
                CharSequence thatNext = thatIterator.next();
                if (!contentEquals(thisNext, thatNext)) {
                    relative.addFirst("..");
                    relative.addLast(thatNext);
                    break;
                }
            } else {
                relative.addFirst("..");
                break;
            }
        }
        // 自身剩下的转为..
        while (thisIterator.hasNext()) {
            thisIterator.next();
            relative.addFirst("..");
        }
        // 对方剩下的追加在路径后方
        while (thatIterator.hasNext()) {
            relative.addLast(thatIterator.next());
        }
        names = relative;
        return this;
    }

    /**
     * 尽量规范化，不抛异常
     */
    public PathBuilder tryNormalize() {
        LinkedList<CharSequence> normalized = new LinkedList<>();
        for (CharSequence name : names) {
            if ("".contentEquals(name) ||
                    ".".contentEquals(name)) {
            } else if ("..".contentEquals(name)) {
                if (normalized.isEmpty() || "..".contentEquals(normalized.getLast())) {
                    normalized.addLast("..");
                } else {
                    normalized.removeLast();
                }
            } else {
                normalized.addLast(name);
            }
        }
        names = normalized;
        return this;
    }

    /**
     * 是否已规范化
     */
    public boolean isNormalized() {
        for (CharSequence name : names) {
            if ("".contentEquals(name) ||
                    ".".contentEquals(name)) {
                return false;
            } else if ("..".contentEquals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 规范化
     */
    public PathBuilder normalize() {
        LinkedList<CharSequence> normalized = new LinkedList<>();
        for (CharSequence name : names) {
            if ("".contentEquals(name) ||
                    ".".contentEquals(name)) {
            } else if ("..".contentEquals(name)) {
                normalized.removeLast();
            } else {
                normalized.addLast(name);
            }
        }
        names = normalized;
        return this;
    }

    /**
     * 规范化
     */
    public PathBuilder normalized() {
        return isNormalized() ? this : normalize();
    }

    /**
     * 路径字符串长度
     */
    public int length() {
        if (names.isEmpty()) {
            return startsWithSeparator() ? 1 : 0;
        }
        return (startsWithSeparator() ? 1 : 0) +
                names.stream().mapToInt(CharSequence::length).sum() +
                (names.size() - 1) +
                (endsWithSeparator() ? 1 : 0);
    }

    public StringBuilder build() {
        return appendTo(new StringBuilder(length()));
    }

    public StringBuilder appendTo(StringBuilder buffer) {
        if (startsWithSeparator()) {
            buffer.append(separatorChar);
        }
        if (names.isEmpty()) {
            return buffer;
        }
        for (CharSequence name : names) {
            buffer.append(name).append(separatorChar);
        }
        buffer.setLength(buffer.length() - 1);
        if (endsWithSeparator()) {
            buffer.append(separatorChar);
        }
        return buffer;
    }

    public String toString() {
        return build().toString();
    }

    public int getNameCount() {
        return names.size();
    }

    public CharSequence getName(int index) {
        return names.get(index);
    }

    public CharSequence getLastName() {
        return names.getLast();
    }

    public LinkedList<CharSequence> getNames() {
        return names;
    }

    public Iterator<CharSequence> iterator() {
        return names.iterator();
    }

    public Iterator<CharSequence> descendingIterator() {
        return names.descendingIterator();
    }

    public ListIterator<CharSequence> listIterator() {
        return names.listIterator();
    }

    public Stream<CharSequence> stream() {
        return names.stream();
    }

    @Override
    protected PathBuilder clone() {
        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.separatorChar = separatorChar;
        pathBuilder.startsWithSeparator = startsWithSeparator;
        pathBuilder.endsWithSeparator = endsWithSeparator;
        pathBuilder.names = new LinkedList<>(names);
        return pathBuilder;
    }
}
