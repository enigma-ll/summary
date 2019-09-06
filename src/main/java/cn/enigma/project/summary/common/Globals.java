package cn.enigma.project.summary.common;

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
}
