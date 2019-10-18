package cn.enigma.project.summary.test.service;

import cn.enigma.project.jpa.query.partial.QueryColumn;
import lombok.Data;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:39
 * Modified By:
 * Description:
 */
@Data
public class TestOneBO {

    @QueryColumn
    private String columnOne;

    @QueryColumn
    private String columnSix;

    public TestOneBO(String columnOne, String columnSix) {
        this.columnOne = columnOne;
        this.columnSix = columnSix;
    }
}
