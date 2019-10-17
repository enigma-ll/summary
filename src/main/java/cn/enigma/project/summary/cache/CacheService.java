package cn.enigma.project.summary.cache;

import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

/**
 * Created with Intellij IDEA.
 *
 * @author luzhihao
 * Create: 2018-05-13 下午6:08
 * Modified By:
 * Description: 定义一个通用缓存接口
 */
public interface CacheService<T> {

    /**
     * 将某个获取数据的执行任务缓存起来（过期时间默认为构造方法里面的时间，单位为ms）
     *
     * @param key              任务key
     * @param dataTask             获取数据FutureTask
     * @param resultFunction   获取结果task方法（自定义使用Future.get()还是Future.get(long timeout, TimeUnit unit)）
     * @param exceptionHandler 任务执行异常转换
     * @return 任务结果
     */
    CacheResult<T> compute(String key, FutureTask<T> dataTask, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler);

    /**
     * 将某个获取数据的执行任务缓存起来（过期时间默认为构造方法里面的时间，单位为ms）
     *
     * @param key              任务key
     * @param dataTask             获取数据FutureTask
     * @param resultFunction   获取结果task方法（自定义使用Future.get()还是Future.get(long timeout, TimeUnit unit)）
     * @param exceptionHandler 任务执行异常转换
     * @param expire           定义任务缓存过期时间，0为永不过期
     * @return 任务结果
     */
    CacheResult<T> compute(String key, FutureTask<T> dataTask, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler, long expire);

    /**
     * 将某个获取数据的执行任务缓存起来（过期时间默认为构造方法里面的时间，单位为ms）
     *
     * @param key              任务key
     * @param dataTask             获取数据FutureTask
     * @param resultFunction   获取结果task方法（自定义使用Future.get()还是Future.get(long timeout, TimeUnit unit)）
     * @param exceptionHandler 任务执行异常转换
     * @param expire           定义任务缓存过期时间，0为永不过期
     * @param afterRun         任务执行完成得到结果后的执行任务
     * @return 任务结果
     */
    CacheResult<T> compute(String key, FutureTask<T> dataTask, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler, long expire, Runnable afterRun);

    void removeCache(String key, Runnable runnable);
}
