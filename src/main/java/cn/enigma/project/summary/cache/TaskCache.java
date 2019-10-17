package cn.enigma.project.summary.cache;

import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;

import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Created with Intellij IDEA.
 *
 * @author luzhihao
 * Create: 2018-05-13 下午6:08
 * Modified By:
 * Description: 定义一个通用缓存接口
 */
public interface TaskCache<T> {

    /**
     * 将某个获取数据的执行任务缓存起来
     *
     * @param key              任务key
     * @param dataTask         获取数据的Task
     * @param resultFunction   获取结果task方法（自定义使用Future.get()还是Future.get(long timeout, TimeUnit unit)）
     * @param exceptionHandler 任务执行异常转换
     * @param expire           定义任务缓存过期时间，小于等于0为永不过期
     * @return 任务结果
     */
    CacheResult<T> compute(String key, Task<T> dataTask, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler, long expire);

    /**
     * 清理缓存，这里会强制执行，慎重
     *
     * @param key      缓存key
     * @param runnable 清理后执行的任务
     */
    void removeCache(String key, Runnable runnable);
}
