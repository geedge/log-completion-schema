package com.zdjizhi.function;

import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class TimestampTest {

    @Test
    public void timestampToDate() {
        System.out.println(getLocalDateTime(1693905281L, 0, "Asia/Shanghai"));
        System.out.println(getLocalDateTime(1693905281048L, 3, "Asia/Shanghai"));
        System.out.println(getLocalDateTime(1693905281048L, 6, "Asia/Shanghai"));
        System.out.println(getLocalDateTime(1693905281048L, 9, "UTC+03:00"));
        System.out.println(getZoneDateTime(1693905281L, 3, "Asia/Shanghai"));
        System.out.println(getZoneDateTime(1693905281048L, 6, "Asia/Shanghai"));
    }


    @Test
    public void timestampConversion() {
        long nanosTimestamp = 1630988475000000000L; // 纳秒级时间戳
        System.out.println("纳秒级时间戳: " + timestampToSeconds(nanosTimestamp));

        long microsTimestamp = 1630988475000000L; // 微秒级时间戳
        System.out.println("微秒级时间戳: " + timestampToSeconds(microsTimestamp));

        long millisTimestamp = 1693969952127L; // 微秒级时间戳
        System.out.println("毫秒级时间戳: " + timestampToSeconds(millisTimestamp));

        long errorTimestamp = 169396995L; // 微秒级时间戳
        System.out.println("异常时间戳: " + timestampToSeconds(errorTimestamp));
    }

    private Long timestampToSeconds(long timestamp) {
        int timestampLength = Long.toString(timestamp).length();
        switch (timestampLength) {
            case 13:
                return TimeUnit.MILLISECONDS.toSeconds(timestamp);
            case 16:
                return TimeUnit.MICROSECONDS.toSeconds(timestamp);
            case 19:
                return TimeUnit.NANOSECONDS.toSeconds(timestamp);
            default:
//                throw new RuntimeException("This timestamp:" + timestamp + " format is not nanosecond, microsecond, millisecond, or second");
                return timestamp;
        }
    }

    private String getLocalDateTime(Long timestamp, int precision, String timeZone) {
        boolean isMillis = String.valueOf(timestamp).length() > 10;
        String timePattern = "yyyy-MM-dd HH:mm:ss";
        if (precision > 0) {
            String s = String.join("", Collections.nCopies(precision, "S"));
            timePattern = String.join(".", timePattern, s);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timePattern);

        Instant instant;
        if (isMillis) {
            instant = Instant.ofEpochMilli(timestamp);
        } else {
            instant = Instant.ofEpochSecond(timestamp);
        }
        return LocalDateTime.ofInstant(instant, ZoneId.of(timeZone)).format(formatter);
    }


    private String getZoneDateTime(Long timestamp, int precision, String timeZone) {
        boolean isMillis = String.valueOf(timestamp).length() > 10;
        String timePattern = "yyyy-MM-dd'T'HH:mm:ss.";
        if (precision > 0) {
            String s = String.join("", Collections.nCopies(precision, "S"));
            timePattern = String.join("", timePattern, s, "XXX");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timePattern);

        Instant instant;
        if (isMillis) {
            instant = Instant.ofEpochMilli(timestamp);
        } else {
            instant = Instant.ofEpochSecond(timestamp);
        }
        return ZonedDateTime.ofInstant(instant, ZoneId.of(timeZone)).format(formatter);
    }
}
