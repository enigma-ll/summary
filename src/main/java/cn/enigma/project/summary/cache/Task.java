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
    private String name;
    private boolean coverOthers = false;
    private int priority = 0;

    public Task(String name, boolean coverOthers, Callable<T> callable) {
        super(callable);
        this.name = name;
        this.coverOthers = coverOthers;
    }

    public Task(String name, boolean coverOthers, int priority, Callable<T> callable) {
        super(callable);
        this.name = name;
        this.coverOthers = coverOthers;
        this.priority = priority;
    }

    public Task(String name, Runnable runnable, T result) {
        super(runnable, result);
    }
}
