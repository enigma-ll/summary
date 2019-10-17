package cn.enigma.project.summary.cache.impl;

import cn.enigma.project.summary.cache.Task;
import cn.enigma.project.summary.cache.TaskCache;
import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Slf4j
public class DefaultTaskCache<T> implements TaskCache<T> {

    private static final long EXPIRE_UNLIMITED = 0L;

    // 存储获取数据task以及定时清除任务task
    private ConcurrentHashMap<String, Cache<T>> taskCache = new ConcurrentHashMap<>(1000);
    // 更新任务时进行加锁处理
    private ConcurrentHashMap<String, Lock> keyLockMap = new ConcurrentHashMap<>(1000);
    // 定时器线程池，用于清除过期缓存
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    @Override
    public CacheResult<T> compute(String key, Task<T> dataTask, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler, long expire) {
        Future<T> future = getFuture(key, dataTask, expire);
        CacheResult<T> cacheResult = new CacheResult<>();
        try {
            cacheResult.setResult(resultFunction.apply(future));
        } catch (Exception e) {
            cacheResult.setException(exceptionHandler.apply(e));
        }
        return cacheResult;
    }

    /**
     * 添加缓存or获取缓存任务future
     *
     * @param key      key
     * @param dataTask 任务
     * @param expire   过期时间
     * @return 任务
     */
    private Future<T> getFuture(String key, Task<T> dataTask, long expire) {
        Lock keyLock = getLock(key);
        keyLock.lock();
        Cache<T> cache;
        try {
            cache = taskCache.get(key);
            if (cache == null) {
                // 如果缓存中没有内容，则直接添加到缓存中
                cache = new Cache<>(dataTask);
                Cache<T> putIfAbsent = taskCache.putIfAbsent(key, cache);
                if (null == putIfAbsent) {
                    // 添加之后，开启任务执行，设置过期清理task
                    dataTask.run();
                    setExpire(key, cache, expire);
                }
            } else {
                // 如果已存在任务，判断是否覆盖新的任务
                if (coverTask(cache.dataTask, dataTask)) {
                    cache = new Cache<>(dataTask);
                    taskCache.put(key, cache);
                    dataTask.run();
                    setExpire(key, cache, expire);
                    return cache.getDataTask();
                }
            }
            log.debug("key {}, taskName {}", key, cache.getDataTask().getName());
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
            Future expireTask = executor.schedule(() -> removeExpiredCache(key, cache), expire, TimeUnit.MILLISECONDS);
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
    private void removeExpiredCache(String key, Cache<T> expiredCache) {
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
            log.debug("removeExpiredCache key {}, target task{}, expire task{}", key, targetTask.getName(), expireTask.getName());
            // 不同名任务不清除
            if (!targetTask.getName().equals(expireTask.getName())) {
                log.debug("removeExpiredCache 1");
                return;
            }
            // 设置了不覆盖并且现有任务设置的可覆盖，则不清除
            if (!expireTask.isCoverOthers() && targetTask.isCoverOthers()) {
                log.debug("removeExpiredCache 2");
                return;
            }
            log.debug("removeExpiredCache remove key {}", key);
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
    public void removeCache(String key, Runnable runnable) {
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
            runnable.run();
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
