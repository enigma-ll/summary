package cn.enigma.project.summary.common.util;

/**
 * @author luzh
 * Create: 2019/9/5 下午4:33
 * Modified By:
 * Description:
 */
public class StringUtil {

    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
