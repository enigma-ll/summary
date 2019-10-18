package cn.enigma.project.summary.test.check;

import java.lang.annotation.*;

/**
 * @author luzh
 * Create: 2019-10-18 12:15
 * Modified By:
 * Description:
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckRepeat {

    /**
     * @return 属性名称
     */
    String name();

    /**
     * @return 属性对应的数据库实体名称
     */
    String tableColumn();
}
