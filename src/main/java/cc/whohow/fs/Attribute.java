package cc.whohow.fs;

import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Objects;

/**
 * 属性
 */
public interface Attribute<V> {
    static String toString(Attribute<?> attribute) {
        return attribute.name() + ": " + attribute.getAsString();
    }

    /**
     * 属性名
     */
    String name();

    /**
     * 属性值
     */
    V value();

    default Long getAsLong() {
        throw new UnsupportedOperationException();
    }

    default FileTime getAsFileTime() {
        throw new UnsupportedOperationException();
    }

    default Date getAsDate() {
        FileTime fileTime = getAsFileTime();
        if (fileTime == null) {
            return null;
        }
        return Date.from(fileTime.toInstant());
    }

    default String getAsString() {
        return Objects.toString(value(), null);
    }
}
