package cn.enigma.project.summary.common.controller.trace.annotation;

import java.lang.annotation.*;

/**
 * @author luzh
 * Create: 2019-08-21 10:38
 * Modified By:
 * Description:
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpTrace {

    /**
     * @return 是否打印接口请求内容
     */
    boolean loggingRequest() default true;

    /**
     * @return 是否打印接口返回内容
     */
    boolean loggingResponse() default true;
}
