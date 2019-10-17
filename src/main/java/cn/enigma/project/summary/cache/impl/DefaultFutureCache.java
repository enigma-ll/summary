package cn.enigma.project.summary.cache.impl;

import cn.enigma.project.summary.cache.FutureCache;
import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
public class DefaultFutureCache<T> implements FutureCache<T> {

    private static final long EXPIRE_UNLIMITED = 0L;

    private boolean runExpireTask(long expire) {
        return expire > EXPIRE_UNLIMITED;
    }

    // 存储获取数据task以及定时清除任务task
    private ConcurrentHashMap<String, Cache<T>> taskCache = new ConcurrentHashMap<>(1000);
    // 定时器线程池，用于清除过期缓存
    private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    private Future<T> getFuture(String key, FutureTask<T> dataTask, long expire, boolean coverTask) {
        Cache<T> cache;
        if (coverTask) {
            cache = new Cache<>(dataTask);
            taskCache.put(key, cache);
            dataTask.run();
            setExpire(key, cache, expire);
            return cache.getMainTask();
        }
        cache = taskCache.get(key);
        if (cache == null) {
            cache = new Cache<>(dataTask);
            Cache<T> putIfAbsent = taskCache.putIfAbsent(key, cache);
            if (null == putIfAbsent) {
                dataTask.run();
                setExpire(key, cache, expire);
            }
        }
        return cache.getMainTask();
    }

    private void setExpire(String key, Cache<T> cache, long expire) {
        if (runExpireTask(expire)) {
            Future expireTask = executor.schedule(() -> removeCache(key, () -> {}), expire, TimeUnit.MILLISECONDS);
            cache.setExpireTask(expireTask);
        }
    }

    @Override
    public CacheResult<T> compute(String key, FutureTask<T> dataTask, boolean coverTask, FutureFunction<Future<T>, T> resultFunction,
                                  Function<Exception, Exception> exceptionHandler, long expire) {
        Future<T> future = getFuture(key, dataTask, expire, coverTask);
        CacheResult<T> cacheResult = new CacheResult<>();
        try {
            cacheResult.setResult(resultFunction.apply(future));
        } catch (Exception e) {
            cacheResult.setException(exceptionHandler.apply(e));
        }
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
