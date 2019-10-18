package cn.enigma.project.summary.task;

import java.util.concurrent.Future;

/**
 * Created with Intellij IDEA.
 *
 * @author luzhihao
 * Create: 2018-05-13 下午6:08
 * Modified By:
 * Description: 定义一个通用缓存接口
 */
public interface TaskCompute<T> {

    /**
     * 计算任务，如果任务存在缓存，根据task的name和cover属性判断是否替换属性还是从缓存中直接读取任务
     *
     * @param key      任务key
     * @param task     获取数据的Task
     * @param getValue 获取task结果执行的方法（自定义使用Future.get()还是Future.get(long timeout, TimeUnit unit)）
     * @param expire   定义任务缓存过期时间，小于等于0为永不过期
     * @return 任务结果
     */
    TaskResult<T> compute(String key, Task<T> task, TaskFunction<Future<T>, T> getValue, long expire);

    /**
     * 批量计算任务
     *
     * @param keys     任务key
     * @param task     获取数据的Task
     * @param getValue 获取task结果执行的方法（自定义使用Future.get()还是Future.get(long timeout, TimeUnit unit)）
     * @param expire   定义任务缓存过期时间，小于等于0为永不过期
     * @return 任务结果
     */
    TaskResult<T> batchCompute(String[] keys, Task<T> task, TaskFunction<Future<T>, T> getValue, long expire);

    /**
     * 清理缓存，这里会强制执行，慎用
     *
     * @param key      缓存key
     * @param runnable 清理后执行的任务
     */
    void removeCache(String key, Runnable runnable);
}
