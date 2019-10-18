package cn.enigma.project.summary.test.controller.req;

import cn.enigma.project.summary.test.check.Checking;
import cn.enigma.project.summary.test.check.CheckRequest;
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
    @Checking(name = "姓名", tableColumn = "name")
    private String name;
    @Checking(name = "年龄", tableColumn = "age")
    private String age;
}
