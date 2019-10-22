package cn.enigma.project.summary.test.check;

import cn.enigma.project.common.task.TaskType;

/**
 * @author luzh
 * Create: 2019-10-18 18:39
 * Modified By:
 * Description:
 */
public enum AddCheckTaskType implements TaskType {
    QUERY("QUERY"), ADD("ADD");

    AddCheckTaskType(String type) {
        this.type = type;
    }

    String type;

    @Override
    public String type() {
        return this.type;
    }
}
