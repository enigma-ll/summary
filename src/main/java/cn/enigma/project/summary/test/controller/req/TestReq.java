package cn.enigma.project.summary.test.controller.req;

import cn.enigma.project.summary.test.check.CheckRequest;
import cn.enigma.project.summary.test.check.Checking;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author luzh
 * Create: 2019-10-18 10:31
 * Modified By:
 * Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestReq extends CheckRequest {
    @Checking(name = "姓名", tableColumn = "name", checkType = Checking.CheckType.NOT_EXIST)
    private String name;
    @Checking(name = "年龄", tableColumn = "age", checkType = Checking.CheckType.NOT_EXIST)
    private String age;
    @Checking(name = "父id", tableColumn = "id", checkType = Checking.CheckType.EXIST)
    private Integer parentId;
}
