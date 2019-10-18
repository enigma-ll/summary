package cn.enigma.project.summary.test.check;

import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.jpa.entity.BaseEntity;
import cn.enigma.project.jpa.query.partial.PartQuery;
import cn.enigma.project.summary.task.Task;
import cn.enigma.project.summary.task.TaskCompute;
import cn.enigma.project.summary.task.TaskResult;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author luzh
 * Create: 2019-10-18 11:44
 * Modified By:
 * Description:
 */
public class CrudCheckDataUtil {

    private static final String SEPARATOR = ":";
    private static final String QUERY_TASK_NAME_PREFIX = "query";
    private static final String ADD_TASK_NAME_PREFIX = "add";

    private static final long EXPIRE_TIME = 1000L;

    public static <T extends BaseEntity, R extends CheckRequest> CheckResult<T> addEntity(String operation,
                                                                                          Class<T> entity,
                                                                                          TaskCompute<T> taskCompute,
                                                                                          R request,
                                                                                          EntityManager entityManager,
                                                                                          Function<R, T> addFunction) {
        List<CheckInfo<String>> pendingCheckList = getCheckInfo(request);
        int len = pendingCheckList.size();
        String[] keys = new String[len];
        // for循环依次执行检查任务
        for (int i = 0; i < len; i++) {
            CheckInfo<String> checkInfo = pendingCheckList.get(i);
            //  每一个要检测的数据确保要生成唯一的key
            String key = generateTaskKey(operation, checkInfo, entity);
            keys[i] = key;
            Task<T> queryTask = generateQueryTask(operation, checkInfo, entityManager, entity);
            // 以具体的数据生成key进行缓存任务，获取任务使用Future.get()一直等待直至返回结果
            TaskResult<T> queryTaskResult = taskCompute.compute(key, queryTask, Future::get, EXPIRE_TIME);
            if (queryTaskResult.hasResult()) {
                return new CheckResult<>(checkInfo, queryTaskResult);
            }
        }
        CheckInfo<R> checkInfo = new CheckInfo<>(request.getClass().getSimpleName(), request, request.getClass().getSimpleName());
        Task<T> addTask = generateAddTask(request, addFunction);
        // 这里批量添加后，之前的同名查询任务会被addEntityTask任务覆盖，也就是说后续有新的添加请求进来时，先走查询代码，
        // 会发现某个实体属性的某个值已经存在了添加任务，然后直接等待操作结果就行，这样就避免了添加重复数据
        TaskResult<T> addTaskResult = taskCompute.batchCompute(keys, addTask, Future::get, EXPIRE_TIME);
        return new CheckResult<>(checkInfo, addTaskResult);
    }

    /**
     * 通过反射和属性注解，得到待检测的属性信息
     *
     * @param request 请求类，需要检测的属性用@CheckRepeat注解标注
     * @return 待检验的信息列表
     */
    private static List<CheckInfo<String>> getCheckInfo(CheckRequest request) {
        List<CheckInfo<String>> list = new ArrayList<>();
        Field[] fields = request.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Checking.class)) {
                try {
                    Checking checking = field.getAnnotation(Checking.class);
                    Object value = field.get(request);
                    if (null == value || "".equals(value.toString())) {
                        continue;
                    }
                    list.add(new CheckInfo<>(checking.name(), value.toString(), checking.tableColumn()));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return list;
    }

    /**
     * 生成查询任务key
     * 加上前缀防止多个属性有相同的值造成缓存污染
     *
     * @param operation 操作名称
     * @param checkInfo 检测内容
     * @return 任务key
     */
    private static <T extends BaseEntity> String generateTaskKey(String operation, CheckInfo checkInfo, Class<T> entity) {
        return operation + SEPARATOR + entity.getSimpleName() + SEPARATOR + checkInfo.getAttributeName() + SEPARATOR + checkInfo.getValue();
    }

    /**
     * 构造查询task
     *
     * @param operation     操作名称
     * @param checkInfo     检测内容
     * @param entityManager em
     * @param entity        实体class
     * @param <T>           数据库实体泛型
     * @return task
     */
    private static <T extends BaseEntity> Task<T> generateQueryTask(String operation, CheckInfo<String> checkInfo,
                                                                    EntityManager entityManager, Class<T> entity) {
        Callable<T> queryTaskCallable = () -> generateQueryCallable(entityManager, checkInfo.getAttributeName(), checkInfo.getValue(), entity);
        // 查询任务级别比较低，如果某个数据已经存在查询任务则会直接等待执行结果，避免重复查询
        Task<T> task = taskComplete(QUERY_TASK_NAME_PREFIX + SEPARATOR + checkInfo.getAttributeName(), false, queryTaskCallable);
        task.setTaskType(AddCheckTaskType.QUERY);
        return task;
    }

    /**
     * 查询数据库
     *
     * @param entityManager em
     * @param attributeName 查询的属性名
     * @param value         value
     * @param entity        数据库实体
     * @param <T>           数据库实体泛型
     * @return 数据
     */
    private static <T extends BaseEntity> T generateQueryCallable(EntityManager entityManager, String attributeName, String value, Class<T> entity) {
        Specification<T> specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(attributeName), value);
        List<T> list = PartQuery.statisticsQuery(entityManager, entity, entity, specification);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 构造任务
     *
     * @param name        任务名称
     * @param coverOthers 是否覆盖其他不同名任务
     * @param callable    获取数据的任务代码
     * @param <T>         数据泛型
     * @return task
     */
    private static <T> Task<T> taskComplete(String name, boolean coverOthers, Callable<T> callable) {
        return Task.complete(name, coverOthers, callable);
    }

    /**
     * 构造添加任务
     *
     * @param request     请求数据
     * @param addFunction 添加数据方法
     * @param <T>         数据库实体泛型
     * @param <R>         请求数据泛型
     * @return task
     */
    private static <T extends BaseEntity, R extends CheckRequest> Task<T> generateAddTask(R request, Function<R, T> addFunction) {
        // 构造添加数据的任务名称
        String addTaskName = ADD_TASK_NAME_PREFIX + SEPARATOR + request.getClass().getSimpleName() + SEPARATOR + request.hashCode();
        Task<T> addTask = taskComplete(addTaskName, true, () -> addFunction.apply(request));
        addTask.setTaskType(AddCheckTaskType.ADD);
        return addTask;
    }

    /**
     * 获取最终数据，如果发现query有返回数据，则抛出异常
     *
     * @param checkResult 检测结果
     * @param request     接口请求参数
     * @param <T>         数据库实体泛型
     * @param <R>         请求数据class泛型
     * @return 数据库实体
     * @throws Exception 出现重复数据抛出的异常
     */
    public static <T extends BaseEntity, R extends CheckRequest> T getResult(CheckResult<T> checkResult, R request) throws Exception {
        if (checkResult.getCheckInfo().getValue().equals(request)) {
            return checkResult.getTaskResult().result();
        }
        TaskResult<T> taskResult = checkResult.getTaskResult();
        if (taskResult.getOriginalTask().getTaskType().type().equals(AddCheckTaskType.QUERY.type()) && taskResult.hasResult()) {
            CheckInfo checkInfo = checkResult.getCheckInfo();
            throw new GlobalException(checkInfo.getName() + "【" + checkInfo.getValue() + "】已存在！");
        }
        return checkResult.getTaskResult().result();
    }


}
