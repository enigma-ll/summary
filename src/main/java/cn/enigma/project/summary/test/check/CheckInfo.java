package cn.enigma.project.summary.test.check;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author luzh
 * Create: 2019-10-18 13:44
 * Modified By:
 * Description:
 */
@Data
@AllArgsConstructor
class CheckInfo<T> {
    private String name;
    private T value;
    private String attributeName;
    private Checking.CheckType checkType;
}
