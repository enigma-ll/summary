package cn.enigma.project.summary.common.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author luzh
 * Create: 2019-06-04 18:18
 * Modified By:
 * Description:
 */
@Slf4j
@Component
public class SystemInit {

    @PostConstruct
    public void dataInitialing() {
        log.debug("{} dataInitialing...", getClass().getName());
        Class<? extends SystemInit> clazz = getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (isInitDataMethod(method)) {
                log.debug("do init: {}", getClass().getName() + "." + method.getName());
                new Thread(() -> {
                    try {
                        method.invoke(this);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("by luzh-> {}:{}.", clazz.getName(), e.getMessage(), e);
                    }
                }).start();
            }
        }
    }

    private boolean isInitDataMethod(Method method) {
        return method.getName().startsWith("init")  // 方法以init开头
                && Modifier.isPublic(method.getModifiers())  // public方法
                && Void.TYPE.equals(method.getReturnType())  // 返回类型为void
                && !method.isVarArgs()  // 无参数
                && !Modifier.isAbstract(method.getModifiers());  // 非抽象方法
    }
}
