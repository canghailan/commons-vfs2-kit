package cc.whohow.fs.util;

public class PathParser {
    private NameIterator iterator;

    public PathParser(CharSequence path) {
        this(path, '/');
    }

    public PathParser(CharSequence path, char separatorChar) {
        if (path != null && path.length() > 0) {
            this.iterator = new NameIterator(path, separatorChar);
        }
    }

    public static String getExtension(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        int index = name.lastIndexOf('.');
        // index == 0 不是扩展名
        if (0 < index && index < name.length() - 1) {
            return name.substring(index + 1);
        }
        return "";
    }

    public String getName(int index) {
        if (iterator == null) {
            return "";
        }
        iterator.absolute(index);
        if (iterator.hasNext()) {
            return iterator.next().toString();
        }
        return "";
    }

    public String getNameWithSeparator(int index) {
        if (iterator == null) {
            return "";
        }
        iterator.absolute(index);
        if (iterator.hasNext()) {
            CharSequence name = iterator.next();
            if (iterator.endsWithSeparator() || iterator.hasNext()) {
                return name.toString() + iterator.getSeparatorChar();
            } else {
                return name.toString();
            }
        }
        return "";
    }

    public String getLastName() {
        if (iterator == null) {
            return "";
        }
        if (iterator.afterLast().hasPrevious()) {
            return iterator.previous().toString();
        }
        return "";
    }

    public String getParent() {
        if (iterator == null) {
            return null;
        }
        if (iterator.afterLast().hasPrevious()) {
            return iterator.getNames()
                    .subSequence(0, iterator.previousSeparatorIndex() + 1)
                    .toString();
        }
        return null;
    }

    public String getExtension() {
        return getExtension(getLastName());
    }

    public int getNameCount() {
        if (iterator == null) {
            return 0;
        }
        return iterator.afterLast().currentIndex();
    }
}
