package cn.enigma.project.summary.cache.impl;

import cn.enigma.project.summary.cache.CacheService;
import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
public class MemoryCacheImpl<T> implements CacheService<T> {

    private static final long EXPIRE_UNLIMITED = 0L;

    private long expireTime;

    public MemoryCacheImpl(long expireTime) {
        this.expireTime = expireTime;
    }

    private boolean runExpireTask(long expire) {
        return expire > EXPIRE_UNLIMITED;
    }

    // 存储获取数据task以及定时清除任务task
    private ConcurrentHashMap<String, Cache<T>> taskCache = new ConcurrentHashMap<>(1000);
    // 定时器线程池，用于清除过期缓存
    private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Future<T> getFuture(String key, FutureTask<T> task, long expire) {
        Cache<T> cache = taskCache.get(key);
        if (cache == null) {
            cache = new Cache<>(task);
            Cache<T> putIfAbsent = taskCache.putIfAbsent(key, cache);
            if (null == putIfAbsent) {
                task.run();
                if (runExpireTask(expire)) {
                    Future expireTask = executor.schedule(() -> taskCache.remove(key), expire, TimeUnit.MILLISECONDS);
                    cache.setExpireTask(expireTask);
                }
            }
        }
        return cache.getMainTask();
    }

    @Override
    public CacheResult<T> compute(String key, FutureTask<T> task, FutureFunction<Future<T>, T> resultFunction,
                                  Function<Exception, Exception> exceptionHandler) {
        return compute(key, task, resultFunction, exceptionHandler, expireTime);
    }

    @Override
    public CacheResult<T> compute(String key, FutureTask<T> task, FutureFunction<Future<T>, T> resultFunction,
                                  Function<Exception, Exception> exceptionHandler, long expire) {
        Future<T> future = getFuture(key, task, expire);
        CacheResult<T> cacheResult = new CacheResult<>();
        try {
            cacheResult.setResult(resultFunction.apply(future));
        } catch (Exception e) {
            cacheResult.setException(exceptionHandler.apply(e));
        }
        return cacheResult;
    }

    @Override
    public CacheResult<T> compute(String key, FutureTask<T> task, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler, long expire, Runnable afterRun) {
        CacheResult<T> cacheResult = compute(key, task, resultFunction, exceptionHandler, expire);
        new Thread(afterRun).start();
        return cacheResult;
    }

    @Override
    public void removeCache(String key, Runnable runnable) {
        log.info("removeCache {}", key);
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
        private Future<T> mainTask;

        Cache(Future<T> mainTask) {
            this.mainTask = mainTask;
        }
    }
}
