package cn.enigma.project.summary.common.util.file;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author luzh
 * Create: 2019-08-13 14:41
 * Modified By:
 * Description:
 */
@Slf4j
public class JsonUtil {

    private static final Gson GSON = new Gson();

    public static <T> Optional<T> convertJson(String content, Class<T> clazz) {
        try {
            T data = GSON.fromJson(content, clazz);
            if (null != data) {
                return Optional.of(data);
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}
