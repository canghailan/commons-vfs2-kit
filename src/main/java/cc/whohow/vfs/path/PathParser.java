package cc.whohow.vfs.path;

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
        String name = getLastName();
        int index = name.lastIndexOf('.');
        if (index < 1) {
            return "";
        }
        return name.substring(index + 1);
    }

    public int getNameCount() {
        if (iterator == null) {
            return 0;
        }
        return iterator.afterLast().currentIndex();
    }
}
