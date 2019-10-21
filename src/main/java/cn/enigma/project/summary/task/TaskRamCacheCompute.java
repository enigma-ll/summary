package cn.enigma.project.summary.task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TaskRamCacheCompute<T> implements TaskCacheCompute<T> {

    private static final long EXPIRE_UNLIMITED = 0L;

    // 存储获取数据task以及定时清除任务task
    private ConcurrentHashMap<String, Cache<T>> taskCache = new ConcurrentHashMap<>(1000);
    // 更新任务时进行加锁处理
    private ConcurrentHashMap<String, Lock> keyLockMap = new ConcurrentHashMap<>(1000);
    // 定时器线程池，用于清除过期缓存
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public TaskResult<T> compute(String key, Task<T> task, TaskFunction<Future<T>, T> getValue, long expire) {
        Task<T> actualTask = handleTask(key, task, expire);
        TaskResult<T> res = new TaskResult<>(task, actualTask);
        try {
            res.setResult(getValue.getValue(actualTask));
        } catch (Exception e) {
            res.setException(e);
        }
        return res;
    }

    @Override
    public TaskResult<T> batchCompute(String[] keys, Task<T> task, TaskFunction<Future<T>, T> getValue, long expire) {
        Task<T> actualTask = null;
        for (String key : keys) {
            actualTask = handleTask(key, task, expire);
        }
        TaskResult<T> res = new TaskResult<>(task, actualTask);
        try {
            res.setResult(getValue.getValue(actualTask));
        } catch (Exception e) {
            res.setException(e);
        }
        return res;
    }


    /**
     * 添加缓存or获取缓存任务future
     *
     * @param key    key
     * @param task   任务
     * @param expire 过期时间
     * @return 任务
     */
    private Task<T> handleTask(String key, Task<T> task, long expire) {
        log.debug("[{}]开始处理任务：[{}]", key, task);
        Lock keyLock = getLock(key);
        keyLock.lock();
        Cache<T> cache;
        try {
            cache = taskCache.get(key);
            if (cache == null) {
                // 如果缓存中没有内容，则直接添加到缓存中
                cache = new Cache<>(task);
                Cache<T> putIfAbsent = taskCache.putIfAbsent(key, cache);
                if (null == putIfAbsent) {
                    // 添加之后，开启任务执行，设置过期清理task
                    task.run();
                    setExpire(key, cache, expire);
                }
            } else {
                // 如果已存在任务，判断是否覆盖新的任务
                if (coverTask(cache.dataTask, task)) {
                    cache = new Cache<>(task);
                    taskCache.put(key, cache);
                    task.run();
                    setExpire(key, cache, expire);
                    return cache.getDataTask();
                }
            }
            log.debug("[{}]实际执行任务：[{}]", key, cache.getDataTask());
            return cache.getDataTask();
        } finally {
            keyLock.unlock();
        }
    }

    /**
     * 获取每个key对应的锁
     *
     * @param key 缓存key
     * @return 锁
     */
    private Lock getLock(String key) {
        Lock lock = new ReentrantLock();
        Lock keyLock = keyLockMap.putIfAbsent(key, lock);
        if (keyLock == null) {
            keyLock = lock;
        }
        return keyLock;
    }

    /**
     * 设置过期清除task
     *
     * @param key    缓存key
     * @param cache  缓存任务
     * @param expire 过期时间（ms）
     */
    private void setExpire(String key, Cache<T> cache, long expire) {
        if (isRunExpireTask(expire)) {
            Future expireTask = executor.schedule(() -> removeExpiredTask(key, cache), expire, TimeUnit.MILLISECONDS);
            cache.setExpireTask(expireTask);
        }
    }

    /**
     * 是否运行定时清理任务
     *
     * @param expire 过期时长
     * @return 是否开启任务
     */
    private boolean isRunExpireTask(long expire) {
        return expire > EXPIRE_UNLIMITED;
    }

    /**
     * 缓存任务到期，清除缓存
     *
     * @param key          key
     * @param expiredCache 要清除的任务
     */
    private void removeExpiredTask(String key, Cache<T> expiredCache) {
        Lock lock = getLock(key);
        lock.lock();
        try {
            Cache<T> targetCache = taskCache.get(key);
            if (null == targetCache) {
                return;
            }
            // 下面的逻辑其实相当于task的compare方法
            Task<T> targetTask = targetCache.getDataTask();
            Task<T> expireTask = expiredCache.getDataTask();
            log.debug("[{}]清理过期缓存任务：[{}]，当前缓存任务：[{}]", key, expiredCache, targetTask);
            // 不同名任务不清除
            if (!targetTask.getName().equals(expireTask.getName())) {
                return;
            }
            // 设置了不覆盖并且现有任务设置的可覆盖，则不清除
            if (!expireTask.isCoverOthers() && targetTask.isCoverOthers()) {
                return;
            }
            log.debug("[{}]清理过期缓存", key);
            taskCache.remove(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 判断是否覆盖同名的缓存任务
     * 判断逻辑是：有相同的任务名称表示执行的代码一样不去覆盖，不同名且允许覆盖才可以
     *
     * @param cacheTask 已缓存的任务
     * @param newTask   新任务
     * @return 是否覆盖
     */
    private boolean coverTask(Task<T> cacheTask, Task<T> newTask) {
        return !cacheTask.getName().equals(newTask.getName()) && newTask.isCoverOthers();
    }


    @Override
    public void removeCache(String key) {
        Lock lock = keyLockMap.get(key);
        lock.lock();
        try {
            Cache<T> cache = taskCache.get(key);
            if (null == cache) return;
            Future expireTask = cache.getExpireTask();
            if (null == expireTask) return;
            expireTask.cancel(true);
        } finally {
            lock.unlock();
        }
    }

    @Data
    private static class Cache<T> {
        // 清除缓存定时任务
        private Future expireTask;
        // 获取数据主任务
        private Task<T> dataTask;

        Cache(Task<T> dataTask) {
            this.dataTask = dataTask;
        }
    }
}
