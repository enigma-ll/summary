package cn.enigma.project.summary.common.controller.trace;

import cn.enigma.project.summary.common.controller.trace.annotation.HttpTrace;
import cn.enigma.project.summary.common.controller.trace.annotation.Logging;
import cn.enigma.project.summary.common.util.IpHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luzh
 * Create: 2019-08-21 10:49
 * Modified By:
 * Description:
 */
@Component
@Aspect
public class HttpTraceAop {

    private static final Logger log = LoggerFactory.getLogger("HttpTraceAop");

    //    @Pointcut("execution(* com.enigma.project..controller.*.*(..))")
    @Pointcut("@annotation(cn.enigma.project.summary.common.controller.trace.annotation.HttpTrace)")
    public void httpTrace() {
    }

    @Before("httpTrace()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null == servletRequestAttributes) {
            return;
        }
        // 打印请求ip、url等基础信息
        HttpServletRequest request = servletRequestAttributes.getRequest();
        log.info("ip: {}, {}: {}, method: {}.",
                IpHelper.getIpAddress(request),
                request.getMethod(),
                request.getRequestURL().toString(),
                getRequestMethodName(joinPoint));
        try {
            loggingRequest(joinPoint);
        } catch (Throwable e) {
            log.error("{} LOGGER request args error: {}.", getRequestMethodName(joinPoint), e.getMessage(), e);
        }
        MDC.put("time", LocalDateTime.now().toString());
    }

    private String getRequestMethodName(JoinPoint jp) {
        return jp.getTarget().getClass().getSimpleName() + "." + jp.getSignature().getName();
    }

    private void loggingRequest(JoinPoint joinPoint) {
        // 判断是否打印请求参数
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 方法只有添加了注解才打印，默认所有参数都打印出来
        // 如果参数添加注解并设置为false则不打印该请求的参数
        HttpTrace isLoggingMethod = method.getAnnotation(HttpTrace.class);
        // 没加注解或者设置为false时不打印参数
        if (null == isLoggingMethod || !isLoggingMethod.loggingRequest()) {
            return;
        }

        // 打印请求参数
        Parameter[] parameters = method.getParameters();
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = discoverer.getParameterNames(method);
        if (null == parameters || null == parameterNames || parameters.length == 0 || parameterNames.length == 0 || joinPoint.getArgs().length == 0) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        // 前面获取到的参数在数组中一般都是按照方法中的参数顺序排列的，所以直接按照对应位置取名称和值就行
        // 暂未考虑全特殊情况，所以分两种情况打印或者出现数组不对应问题，仅供分析请求数据时简单参考
        if (parameters.length == joinPoint.getArgs().length && parameters.length == parameterNames.length) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (ModelMap.class.getName().equals(parameter.getType().getName())) {
                    continue;
                }
                Logging isLogParam = parameter.getAnnotation(Logging.class);
                if (null == isLogParam || isLogParam.logging()) {
                    map.put(parameterNames[i], null == joinPoint.getArgs()[i] ? "null" : joinPoint.getArgs()[i].toString());
                }
            }
            log.info("{} request args: {}.", getRequestMethodName(joinPoint), map.toString());
        } else {
            log.info("{} request params: {}, values: {}.", getRequestMethodName(joinPoint), parameterNames, Arrays.toString(joinPoint.getArgs()));
        }
    }

    @AfterReturning(returning = "result", pointcut = "httpTrace()")
    public void returning(JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 如果方法所在的类是Controller注解并且方法没有ResponseBody注解的话为返回html页面，不打印返回内容
        Controller controller = method.getDeclaringClass().getAnnotation(Controller.class);
        if (null != controller) {
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if (null == responseBody) {
                return;
            }
        }
        if (Void.TYPE.getTypeName().equals(method.getReturnType().getTypeName())) {
            return;
        }
        // 没加注解或者设置为false时不打印返回内容
        HttpTrace isLogging = method.getAnnotation(HttpTrace.class);
        if (null != isLogging && isLogging.loggingResponse()) {
            log.info("{} result: {}.", getRequestMethodName(joinPoint), null == result ? "null" : result.toString());
        }
    }

    @AfterThrowing(pointcut = "httpTrace()", throwing = "throwable")
    public void throwing(JoinPoint joinPoint, Throwable throwable) {
        log.error("{} error: {}.", getRequestMethodName(joinPoint), throwable.getMessage());
    }
}
