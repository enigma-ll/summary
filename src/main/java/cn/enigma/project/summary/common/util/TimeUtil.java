package cn.enigma.project.summary.common.util;

import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @author luzh
 * Create: 2019-08-23 09:56
 * Modified By:
 * Description:
 */
public class TimeUtil {

    public static final DateTimeFormatter COMMON_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static ZoneOffset systemDefaultZoneId;

    /**
     * long类型时间转成通用格式(yyyy-MM-dd HH:mm:ss)
     *
     * @param dateTime 时间
     * @return str
     */
    public static String formatterLongDateTime(Long dateTime) {
        return COMMON_FORMATTER.format(convertDateTime(dateTime));
    }

    public static LocalDateTime convertDateTime(Long dateTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), getSystemDefaultZoneId());
    }

    public static ZoneOffset getSystemDefaultZoneId() {
        if (systemDefaultZoneId == null) {
//            systemDefaultZoneId = Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now());
            systemDefaultZoneId = ZoneOffset.from(ZonedDateTime.now());
        }
        return systemDefaultZoneId;
    }

    public static Optional convertTimeDefaultFormatter(String time) {
        if (StringUtils.isEmpty(time)) {
            return Optional.empty();
        }
        if (time.length() == 16) {
            time += ":00";
        } else if (time.length() == 10) {
            time += " 00:00:00";
        } else if (time.length() != 19) {
            return Optional.empty();
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(time, COMMON_FORMATTER);
            return Optional.of(dateTime.toInstant(getSystemDefaultZoneId()).toEpochMilli());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        long start = Instant.now().toEpochMilli();
        ZoneId zoneOffset = getSystemDefaultZoneId();
        long end = Instant.now().toEpochMilli();
        System.out.println(Instant.now().toEpochMilli() - start);
        start = end;
        zoneOffset = getSystemDefaultZoneId();
        end = Instant.now().toEpochMilli();
        System.out.println(end - start);
    }
}
