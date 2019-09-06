package cn.enigma.project.summary.test.service;

import cn.enigma.project.summary.jpa.part.PartAnnotation;
import lombok.Data;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:39
 * Modified By:
 * Description:
 */
@Data
//@EqualsAndHashCode(callSuper = false)
public class TestOneBO {

    @PartAnnotation(name = "columnOne")
    private String columnOne;

    private String columnSix;

    public TestOneBO(String columnOne, String columnSix) {
        this.columnOne = columnOne;
        this.columnSix = columnSix;
    }
}
