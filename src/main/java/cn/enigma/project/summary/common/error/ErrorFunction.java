package cn.enigma.project.summary.common.error;

/**
 * @author luzh
 * Create: 2019-05-30 15:22
 * Modified By:
 * Description:
 */
public interface ErrorFunction<O, T, R> {

    R apply(O t, T r);
}
