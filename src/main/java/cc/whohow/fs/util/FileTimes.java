package cc.whohow.fs.util;

import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileTimes {
    private static final FileTime EPOCH = FileTime.fromMillis(0);
    private static final DateTimeFormatter FORMATTER;

    static {
        Map<Long, String> dow = new HashMap<>();
        dow.put(1L, "Mon");
        dow.put(2L, "Tue");
        dow.put(3L, "Wed");
        dow.put(4L, "Thu");
        dow.put(5L, "Fri");
        dow.put(6L, "Sat");
        dow.put(7L, "Sun");
        Map<Long, String> moy = new HashMap<>();
        moy.put(1L, "Jan");
        moy.put(2L, "Feb");
        moy.put(3L, "Mar");
        moy.put(4L, "Apr");
        moy.put(5L, "May");
        moy.put(6L, "Jun");
        moy.put(7L, "Jul");
        moy.put(8L, "Aug");
        moy.put(9L, "Sep");
        moy.put(10L, "Oct");
        moy.put(11L, "Nov");
        moy.put(12L, "Dec");
        FORMATTER = new DateTimeFormatterBuilder()
                .optionalStart()
                .appendText(ChronoField.DAY_OF_WEEK, dow)
                .appendLiteral(' ')
                .optionalEnd()
                .optionalStart()
                .appendText(ChronoField.MONTH_OF_YEAR, moy)
                .appendLiteral(' ')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral(' ')
                .optionalEnd()
                .optionalStart()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR)
                .optionalEnd()
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE)
                .appendLiteral(' ')
                .optionalEnd()
                .optionalStart()
                .appendZoneText(TextStyle.SHORT, Collections.singleton(ZoneId.systemDefault()))
                .appendLiteral(' ')
                .optionalEnd()
                .appendValue(ChronoField.YEAR)
                .optionalStart()
                .appendLiteral("-")
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .optionalEnd()
                .optionalStart()
                .appendLiteral(' ')
                .optionalEnd()
                .optionalStart()
                .appendLiteral('T')
                .optionalEnd()
                .optionalStart()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR)
                .optionalEnd()
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE)
                .optionalEnd()
                .optionalStart()
                .appendLiteral('.')
                .appendValue(ChronoField.MILLI_OF_SECOND)
                .optionalEnd()
                .optionalStart()
                .appendOffsetId()
                .optionalEnd()
                .optionalStart()
                .appendLiteral('[')
                .optionalEnd()
                .optionalStart()
                .parseCaseSensitive()
                .appendZoneRegionId()
                .optionalEnd()
                .optionalStart()
                .appendLiteral(']')
                .optionalEnd()
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .toFormatter()
                .withZone(ZoneId.systemDefault())
                .withResolverStyle(ResolverStyle.SMART);
    }

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
