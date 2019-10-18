package cn.enigma.project.summary.task;

/**
 * @author luzh
 * Create: 2019-10-18 17:07
 * Modified By:
 * Description:
 */
public interface TaskFunction<T, R> {

    R getValue(T t) throws Exception;
}
