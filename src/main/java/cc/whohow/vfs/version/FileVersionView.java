package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class FileVersionView {
    private final String version;
    private final long size;
    private final long lastModifiedTime;
    private final String name;

    public FileVersionView(String name) {
        this("", -1, -1, name);
    }

    public FileVersionView(String version, long size, long lastModifiedTime, String name) {
        this.version = version;
        this.size = size;
        this.lastModifiedTime = lastModifiedTime;
        this.name = name;
    }

    public static String stringify(Object value) {
        return value == null ? null : value.toString();
    }

    public static <V> FileVersionView of(FileVersion<V> version) {
        return of(version, FileVersionView::stringify);
    }

    public static <V> FileVersionView of(FileVersion<V> version, Function<V, String> stringify) {
        try {
            CloudFileObject fileObject = version.getFileObject();
            if (fileObject.isFolder()) {
                return new FileVersionView(fileObject.getName().getURI());
            } else {
                return new FileVersionView(stringify.apply(version.getVersion()), fileObject.getSize(), fileObject.getLastModifiedTime(), fileObject.getName().getURI());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileVersionView parse(String text) {
        int s1 = text.indexOf('\t');
        if (s1 < 0) {
            throw new IllegalArgumentException(text);
        }
        int s2 = text.indexOf('\t', s1 + 1);
        if (s2 < 0) {
            throw new IllegalArgumentException(text);
        }
        int s3 = text.indexOf('\t', s2 + 1);
        if (s3 < 0) {
            throw new IllegalArgumentException(text);
        }
        String version = (s1 == 0) ? "" : text.substring(0, s1);
        String size = (s2 == s1 + 1) ? "" : text.substring(s1 + 1, s2);
        String lastModifiedTime = (s3 == s2 + 1) ? "" : text.substring(s2 + 1, s3);
        String name = text.substring(s3 + 1);
        return new FileVersionView(
                version,
                size.isEmpty() ? -1 : Long.parseLong(size),
                lastModifiedTime.isEmpty() ? -1 : ZonedDateTime.parse(lastModifiedTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli(),
                name
        );
    }

    public String getVersion() {
        return version;
    }

    public long getSize() {
        return size;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(256);
        if (getVersion() != null) {
            buffer.append(getVersion());
        }
        buffer.append('\t');
        if (getSize() >= 0) {
            buffer.append(getSize());
        }
        buffer.append('\t');
        if (getLastModifiedTime() > 0) {
            buffer.append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(getLastModifiedTime()), ZoneId.systemDefault())));
        }
        buffer.append('\t');
        buffer.append(getName());
        return buffer.toString();
    }
}
