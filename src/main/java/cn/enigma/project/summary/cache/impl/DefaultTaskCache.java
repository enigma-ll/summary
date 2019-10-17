package cn.enigma.project.summary.cache.impl;

import cn.enigma.project.summary.cache.TaskCache;
import cn.enigma.project.summary.cache.Task;
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

    private boolean runExpireTask(long expire) {
        return expire > EXPIRE_UNLIMITED;
    }

    // 存储获取数据task以及定时清除任务task
    private ConcurrentHashMap<String, Cache<T>> taskCache = new ConcurrentHashMap<>(1000);
    // 更新任务时进行加锁处理
    private ConcurrentHashMap<String, Lock> keyLockMap = new ConcurrentHashMap<>(1000);
    // 定时器线程池，用于清除过期缓存
    private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


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

    private Future<T> getFuture(String key, Task<T> dataTask, long expire) {
        Lock keyLock = getLock(key);
        keyLock.lock();
        Cache<T> cache;
        try {
            cache = taskCache.get(key);
            if (cache == null) {
                cache = new Cache<>(dataTask);
                Cache<T> putIfAbsent = taskCache.putIfAbsent(key, cache);
                if (null == putIfAbsent) {
                    System.out.println("put cache");
                    dataTask.run();
                    setExpire(key, cache, expire);
                }
            } else {
                if (coverTask(cache.mainTask, dataTask)) {
                    cache = new Cache<>(dataTask);
                    System.out.println("put cache");
                    taskCache.put(key, cache);
                    dataTask.run();
                    setExpire(key, cache, expire);
                    return cache.getMainTask();
                }
            }
            return cache.getMainTask();
        } finally {
            keyLock.unlock();
        }
    }

    private Lock getLock(String key) {
        Lock lock = new ReentrantLock();
        Lock keyLock = keyLockMap.putIfAbsent(key, lock);
        if (keyLock == null) {
            System.out.println("new lock");
            keyLock = lock;
        }
        return keyLock;
    }

    private void setExpire(String key, Cache<T> cache, long expire) {
        if (runExpireTask(expire)) {
            Future expireTask = executor.schedule(() -> removeCache(key, () -> {
            }), expire, TimeUnit.MILLISECONDS);
            cache.setExpireTask(expireTask);
        }
    }

    private boolean coverTask(Task<T> cacheTask, Task<T> newTask) {
        return !cacheTask.getName().equals(newTask.getName()) && newTask.isCoverOthers() && newTask.getPriority() > cacheTask.getPriority();
    }


    @Override
    public void removeCache(String key, Runnable runnable) {
        log.info("removeCache {}", key);
        Lock lock = keyLockMap.remove(key);
        if (null != lock) {
            lock.unlock();
        }
        Cache<T> cache = taskCache.remove(key);
        if (null == cache) return;
        Future expireTask = cache.getExpireTask();
        if (null == expireTask) return;
        expireTask.cancel(true);
        runnable.run();
    }

    @Data
    private static class Cache<T> {
        private Future expireTask;
        private Task<T> mainTask;

        Cache(Task<T> mainTask) {
            this.mainTask = mainTask;
        }
    }
}
