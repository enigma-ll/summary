package cn.enigma.project.summary.cache;

import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created with Intellij IDEA.
 *
 * @author luzhihao
 * Create: 2018-05-13 下午6:08
 * Modified By:
 * Description: 定义一个通用缓存接口
 */
public interface CacheService<T> {

    CacheResult<T> compute(String key, Callable<T> callable, Function<Future<T>, CacheResult<T>> futureGet);

    CacheResult<T> compute(String key, Callable<T> callable, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler);

    void removeCache(String key, Supplier function);
}
