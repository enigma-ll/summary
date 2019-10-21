package cn.enigma.project.summary.test.check;

/**
 * @author luzh
 * Create: 2019-10-21 11:49
 * Modified By:
 * Description:
 */
public interface Function<X, Y, Z> {

    Z apply(X x, Y y) throws Exception;
}
