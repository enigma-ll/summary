package cn.enigma.project.summary.cache.function;

public interface FutureFunction<T, R> {

    R apply(T t) throws Exception;
}
