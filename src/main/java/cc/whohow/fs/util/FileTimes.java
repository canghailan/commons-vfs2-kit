package cc.whohow.fs.util;

import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FileTimes {
    private static final FileTime EPOCH = FileTime.fromMillis(0);

    public static FileTime epoch() {
        return EPOCH;
    }

    public static ZonedDateTime parse(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String stringify(Date dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ZonedDateTime
                .ofInstant(dateTime.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String stringify(FileTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return ZonedDateTime
                .ofInstant(dateTime.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String stringify(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
