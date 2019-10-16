package cn.enigma.project.summary.cache.impl;

import cn.enigma.project.summary.cache.function.FutureFunction;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import cn.enigma.project.summary.cache.CacheService;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Supplier;

public class CacheServiceImpl<T> implements CacheService<T> {

    private ConcurrentHashMap<String, Future<T>> cache = new ConcurrentHashMap<>(1000);

    private Future<T> getFuture(String key, Callable<T> callable) {
        Future<T> future = cache.get(key);
        if (future == null) {
            FutureTask<T> futureTask = new FutureTask<>(callable);
            future = cache.putIfAbsent(key, futureTask);
            if (null == future) {
                futureTask.run();
                future = futureTask;
            }
        }
        return future;
    }

    @Override
    public CacheResult<T> compute(String key, Callable<T> callable, Function<Future<T>, CacheResult<T>> futureResult) {
        Future<T> future = getFuture(key, callable);
        return futureResult.apply(future);
    }

    @Override
    public CacheResult<T> compute(String key, Callable<T> callable, FutureFunction<Future<T>, T> resultFunction, Function<Exception, Exception> exceptionHandler) {
        Future<T> future = getFuture(key, callable);
        CacheResult<T> cacheResult = new CacheResult<>();
        try {
            T t = resultFunction.apply(future);
            cacheResult.setResult(t);
        } catch (Exception e) {
            Exception exception = exceptionHandler.apply(e);
            cacheResult.setException(exception);
        }
        return cacheResult;
    }

    @Override
    public void removeCache(String key, Supplier function) {

    }
}
