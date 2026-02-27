package org.dromara.easyes.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DateUtil {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Map<String, DateTimeFormatterHolder> FORMATTER_CACHE = new ConcurrentHashMap<>();

    /**
     * 记录各pattern的转换方法，提升性能
     */
    private static class DateTimeFormatterHolder {
        DateTimeFormatter formatter;
        Func2<DateTimeFormatter, String, ZonedDateTime> parseFunc;

        public DateTimeFormatterHolder(String formatter) {
            this.formatter = DateTimeFormatter.ofPattern(formatter);
        }
    }

    private static class ZoneFunc implements Func2<DateTimeFormatter, String, ZonedDateTime> {
        private static final Func2<DateTimeFormatter, String, ZonedDateTime> INSTANCE = new ZoneFunc();

        @Override
        public ZonedDateTime apply(DateTimeFormatter formatter, String time) {
            // 尝试按完整格式（含时区）解析
            return ZonedDateTime.parse(time, formatter).withZoneSameInstant(ZONE);
        }
    }

    private static class NoZoneFunc implements Func2<DateTimeFormatter, String, ZonedDateTime> {
        private static final Func2<DateTimeFormatter, String, ZonedDateTime> INSTANCE = new NoZoneFunc();

        @Override
        public ZonedDateTime apply(DateTimeFormatter formatter, String time) {
            // 尝试按 带日期，带时间 的格式解析
            return LocalDateTime.parse(time, formatter).atZone(ZONE);
        }
    }

    private static class NoTimeFunc implements Func2<DateTimeFormatter, String, ZonedDateTime> {
        private static final Func2<DateTimeFormatter, String, ZonedDateTime> INSTANCE = new NoTimeFunc();

        @Override
        public ZonedDateTime apply(DateTimeFormatter formatter, String time) {
            // 尝试按 带日期，不带时间 的格式解析
            LocalDate localDate = LocalDate.parse(time, formatter);
            LocalDateTime localDateTime = localDate.atStartOfDay();
            return localDateTime.atZone(ZONE);
        }
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatterHolder::new).formatter;
    }

    private static DateTimeFormatterHolder getFormatterHolder(String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatterHolder::new);
    }

    public static String format(ZonedDateTime zonedDateTime, String pattern) {
        if (zonedDateTime == null) {
            return null;
        }
        return getFormatter(pattern).format(zonedDateTime);
    }

    public static String format(Date date, String pattern) {
        return format(ZonedDateTime.from(date.toInstant().atZone(ZONE)), pattern);
    }

    public static String format(LocalDate date, String pattern) {
        return format(date.atStartOfDay(ZONE), pattern);
    }

    public static String format(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime.atZone(ZONE), pattern);
    }

    public static ZonedDateTime parseZonedDateTime(String time, String pattern) {
        if (StringUtils.isBlank(time)) {
            return null;
        }
        DateTimeFormatterHolder formatterHolder = getFormatterHolder(pattern);
        if (formatterHolder.parseFunc != null) {
            return formatterHolder.parseFunc.apply(formatterHolder.formatter, time);
        } else {
            return tryParse(time, formatterHolder);
        }
    }

    private static ZonedDateTime tryParse(String time, DateTimeFormatterHolder formatterHolder) {
        DateTimeFormatter formatter = formatterHolder.formatter;
        ZonedDateTime zonedDateTime;
        try {
            // 尝试按完整格式（含时区）解析
            zonedDateTime = ZoneFunc.INSTANCE.apply(formatter, time);
            formatterHolder.parseFunc = ZoneFunc.INSTANCE;
        } catch (DateTimeParseException e) {
            // 如果失败，解析为 LocalDateTime，并使用现在时区
            try {
                zonedDateTime = NoZoneFunc.INSTANCE.apply(formatter, time);
                formatterHolder.parseFunc = NoZoneFunc.INSTANCE;
            } catch (DateTimeParseException e1) {
                zonedDateTime = NoTimeFunc.INSTANCE.apply(formatter, time);
                formatterHolder.parseFunc = NoTimeFunc.INSTANCE;
            }
        }
        return zonedDateTime;
    }

    public static Date parse(String str, String pattern) {
        ZonedDateTime zonedDateTime = parseZonedDateTime(str, pattern);
        if (zonedDateTime == null) {
            return null;
        }
        return Date.from(zonedDateTime.toInstant());
    }

    public static LocalDate parseLocalDate(String str, String pattern) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        ZonedDateTime zonedDateTime = parseZonedDateTime(str, pattern);
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.toLocalDate();
    }

    public static LocalDateTime parseLocalDateTime(String str, String pattern) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        ZonedDateTime zonedDateTime = parseZonedDateTime(str, pattern);
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.toLocalDateTime();
    }
}
