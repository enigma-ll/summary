package cn.enigma.project.summary.common;

import java.util.Optional;

/**
 * @author luzh
 * Create: 2019-05-27 16:44
 * Modified By:
 * Description:
 */
public class Globals {

    public static Throwable getOriginException(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }

    public static Optional<Integer> convertString2Integer(String number) {
        try {
            return Optional.of(new Integer(number));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
