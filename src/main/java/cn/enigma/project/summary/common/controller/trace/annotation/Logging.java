package cn.enigma.project.summary.common.controller.trace.annotation;

import java.lang.annotation.*;

/**
 * @author luzh
 * Create: 2019-08-21 10:41
 * Modified By:
 * Description:
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Logging {
    /**
     * @return 是否打印
     */
    boolean logging() default true;
}
