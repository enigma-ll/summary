package cn.enigma.project.summary.cache.function;

/**
 * @author luzh
 * Create: 2019-10-17 16:24
 * Modified By:
 * Description: jdk默认的方法不支持抛出异常，所以单独封装一个function，支持抛出异常
 */
public interface FutureFunction<T, R> {

    R apply(T t) throws Exception;
}
