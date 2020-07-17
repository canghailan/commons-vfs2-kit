package cc.whohow.fs;

import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * 属性集
 */
public interface Attributes extends Iterable<Attribute<?>> {
    static String toString(Attributes attributes) {
        StringJoiner buffer = new StringJoiner("\n");
        for (Attribute<?> attribute : attributes) {
            buffer.add(attribute.toString());
        }
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    default <T> Optional<T> getValue(String name) {
        return (Optional<T>) get(name).map(Attribute::value);
    }

    default Optional<String> getAsString(String name) {
        return get(name).map(Attribute::getAsString);
    }

    default Optional<Long> getAsLong(String name) {
        return get(name).map(Attribute::getAsLong);
    }

    default Optional<FileTime> getAsFileTime(String name) {
        return get(name).map(Attribute::getAsFileTime);
    }

    default Optional<Date> getAsDate(String name) {
        return get(name).map(Attribute::getAsDate);
    }

    default Optional<? extends Attribute<?>> get(String name) {
        Objects.requireNonNull(name);
        for (Attribute<?> attribute : this) {
            if (name.equals(attribute.name())) {
                return Optional.of(attribute);
            }
        }
        return Optional.empty();
    }
}
