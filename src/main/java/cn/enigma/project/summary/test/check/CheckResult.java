package cn.enigma.project.summary.test.check;

import cn.enigma.project.common.task.TaskResult;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author luzh
 * Create: 2019/10/18 10:19 下午
 * Modified By:
 * Description:
 */
@AllArgsConstructor
@Data
public class CheckResult<T> {
    private CheckInfo checkInfo;
    private TaskResult<T> taskResult;
}
