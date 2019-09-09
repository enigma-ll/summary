package cn.enigma.project.summary.common.util;

import cn.enigma.project.summary.common.Globals;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luzh
 * Create: 2019-06-17 10:30
 * Modified By:
 * Description: 将id字符串和id列表互转util
 */
public class StitchingIdUtil {

    private static final String SPLIT = "-";

    private static String replace(String str) {
        return str.replace("[", "").replace("]", "");
    }

    /**
     * 将 [1]-[2]-[3] 转成 [1,2,3]
     *
     * @param str 字符串
     * @return 列表
     */
    public static List<Integer> convertIntStr2List(String str) {
        return StringUtil.isEmpty(str)
                ? Collections.emptyList()
                : Arrays.stream(str.split(SPLIT)).map(StitchingIdUtil::replace).map(Globals::convertString2Integer)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    /**
     * 将 [1,2,3] 转成 [1]-[2]-[3]
     *
     * @param list 列表
     * @return 字符串
     */
    public static String convertIntList2String(List<Integer> list) {
        return null == list || list.isEmpty() ? "" : list.stream().filter(Objects::nonNull).map(StitchingIdUtil::getSingleStr)
                .collect(Collectors.joining(SPLIT));
    }

    /**
     * 将 [a]-[b]-[c] 转成 [a, b, c]
     *
     * @param str 字符串
     * @return 列表
     */
    public static List<String> convertStringStr2List(String str) {
        return StringUtil.isEmpty(str)
                ? Collections.emptyList()
                : Arrays.stream(str.split(SPLIT)).map(StitchingIdUtil::replace).filter(s -> !StringUtil.isEmpty(s)).collect(Collectors.toList());
    }

    /**
     * 将 [a, b, c] 转成 [a]-[b]-[c]
     *
     * @param list 列表
     * @return 字符串
     */
    public static String convertStringList2String(List<String> list) {
        return null == list || list.isEmpty() ? "" : list.stream().filter(str -> !StringUtil.isEmpty(str)).map(StitchingIdUtil::getSingleStr)
                .collect(Collectors.joining(SPLIT));
    }

    public static String getSingleStr(Object str) {
        Assert.notNull(str, "str不能为空");
        return "[" + str + "]";
    }

    public static void main(String[] args) {
        String str = "[440303008001]";
        System.out.println(convertIntStr2List(str));
        System.out.println(convertStringStr2List(str));
    }
}
