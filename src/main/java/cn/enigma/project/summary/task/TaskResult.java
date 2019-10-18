package cn.enigma.project.summary.task;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author luzh
 * Create: 2019-10-18 17:40
 * Modified By:
 * Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TaskResult<T> extends CacheResult<T> {
    private Task<T> originalTask;
    private Task<T> actualTask;

    public TaskResult(Task<T> originalTask, Task<T> actualTask) {
        this.originalTask = originalTask;
        this.actualTask = actualTask;
    }
}
