package cn.enigma.project.summary.cache;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author luzh
 * Create: 2019-10-17 16:24
 * Modified By:
 * Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Task<T> extends FutureTask<T> {
    // 任务名称，用来判断是不是相同的执行方法
    private String name;
    // 是否覆盖同名的已存在的任务
    private boolean coverOthers = false;

    public Task(String name, boolean coverOthers, Callable<T> callable) {
        super(callable);
        this.name = name;
        this.coverOthers = coverOthers;
    }

    public Task(String name, Runnable runnable, T result) {
        super(runnable, result);
    }

    public static <T> Task<T> complete(String name, boolean coverOthers, Callable<T> callable) {
        return new Task<>(name, coverOthers, callable);
    }
}
