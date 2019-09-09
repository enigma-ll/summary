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

    /**
     * 通用时间格式
     */
    public static final DateTimeFormatter COMMON_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static ZoneOffset systemDefaultZoneId;

    /**
     * long转LocalDateTime
     *
     * @param dateTime utc毫秒值
     * @return LocalDateTime
     */
    public static LocalDateTime convertDateTime(Long dateTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), getSystemDefaultZoneId());
    }

    /**
     * LocalDateTime转long
     *
     * @param dateTime 时间
     * @return utc毫秒值
     */
    public static Long convertDateTime(LocalDateTime dateTime) {
        return dateTime.toInstant(getSystemDefaultZoneId()).toEpochMilli();
    }

    /**
     * 获取系统默认时区
     *
     * @return 时区ZoneOffset
     */
    public static ZoneOffset getSystemDefaultZoneId() {
        if (systemDefaultZoneId == null) {
            // systemDefaultZoneId = Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now());
            systemDefaultZoneId = ZoneOffset.from(ZonedDateTime.now());
        }
        return systemDefaultZoneId;
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss时间转换成LocalDateTime
     *
     * @param time 时间
     * @return LocalDateTime
     */
    public static Optional<LocalDateTime> convertTimeWithDefaultFormatter(String time) {
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
            return Optional.of(dateTime);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
