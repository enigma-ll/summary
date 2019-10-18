package cn.enigma.project.summary.task;

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
    private TaskType taskType;
    /**
     * task名称
     */
    private String name;
    /**
     * 是否覆盖其他不同名task
     */
    private boolean coverOthers = false;

    private Task(String name, boolean coverOthers, Callable<T> callable) {
        super(callable);
        this.name = name;
        this.coverOthers = coverOthers;
    }

    private Task(String name, boolean coverOthers, TaskType taskType, Callable<T> callable) {
        super(callable);
        this.name = name;
        this.coverOthers = coverOthers;
        this.taskType = taskType;
    }

    private Task(Runnable runnable, T result) {
        super(runnable, result);
    }

    public static <T> Task<T> complete(String name, boolean coverOthers, Callable<T> callable) {
        return new Task<>(name, coverOthers, callable);
    }

    public static <T> Task<T> of(String name, boolean coverOthers, TaskType taskType, Callable<T> runner) {
        return new Task<>(name, coverOthers, taskType, runner);
    }
 }
