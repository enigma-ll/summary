package cn.enigma.project.summary.test.check;

import cn.enigma.project.jpa.entity.BaseEntity;
import cn.enigma.project.jpa.part.PartQuery;
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
public class AddCheckUtil {

    private static final String SEPARATOR = ":";
    private static final String QUERY_TASK_NAME_PREFIX = "query";
    private static final String ADD_TASK_NAME_PREFIX = "add_entity";

    public static <T extends BaseEntity, R extends CheckRequest> TaskResult<T> addEntityV1(String operation,
                                                                                           Class<T> entity,
                                                                                           TaskCompute<T> taskCompute,
                                                                                           R request,
                                                                                           EntityManager entityManager,
                                                                                           Function<R, T> addFunction) {
        List<CheckInfo> pendingCheckList = getCheckInfo(request);
        int len = pendingCheckList.size();
        String[] keys = new String[len];
        // for循环依次执行检查任务
        for (int i = 0; i < len; i++) {
            CheckInfo checkInfo = pendingCheckList.get(i);
            //  每一个要检测的数据确保要生成唯一的key
            String key = generateTaskKey(operation, checkInfo);
            keys[i] = key;
            Task<T> queryTask = generateQueryTask(operation, checkInfo, entityManager, entity);
            // 以具体的数据生成key进行缓存任务，获取任务使用Future.get()一直等待直至返回结果，查询任务暂时设置永不过期
            TaskResult<T> queryTaskResult = taskCompute.compute(key, queryTask, Future::get, 0L);
            if (queryTaskResult.hasResult()) {
                return queryTaskResult;
            }
        }
        Task<T> addTask = generateAddTask(request, addFunction);
        // 这里批量添加后，之前的同名查询任务会被addEntityTask任务覆盖，也就是说后续有新的添加请求进来时，先走查询代码，
        // 会发现某个实体属性的某个值已经存在了添加任务，然后直接等待操作结果就行，这样就避免了添加重复数据
        return taskCompute.batchCompute(keys, addTask, Future::get, 1000L);
    }

    /**
     * 通过反射和属性注解，得到待检测的属性信息
     *
     * @param request 请求类，需要检测的属性用@CheckRepeat注解标注
     * @return 待检验的信息列表
     */
    private static List<CheckInfo> getCheckInfo(CheckRequest request) {
        List<CheckInfo> list = new ArrayList<>();
        Field[] fields = request.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(CheckRepeat.class)) {
                try {
                    CheckRepeat checkRepeat = field.getAnnotation(CheckRepeat.class);
                    list.add(new CheckInfo(checkRepeat.name(), field.get(request).toString(), checkRepeat.tableColumn()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
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
    private static String generateTaskKey(String operation, CheckInfo checkInfo) {
        return operation + SEPARATOR + checkInfo.getAttributeName() + SEPARATOR + checkInfo.getValue();
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
    private static <T extends BaseEntity> Task<T> generateQueryTask(String operation, CheckInfo checkInfo,
                                                                    EntityManager entityManager, Class<T> entity) {
        Callable<T> queryTaskCallable = () -> generateQueryCallable(entityManager, checkInfo.getAttributeName(), checkInfo.getValue(), entity);
        // 查询任务级别比较低，如果某个数据已经存在查询任务则会直接等待执行结果，避免重复查询
        Task<T> task = taskComplete(operation + QUERY_TASK_NAME_PREFIX + checkInfo.getAttributeName(), false, queryTaskCallable);
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
}
