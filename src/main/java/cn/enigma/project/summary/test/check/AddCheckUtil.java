package cn.enigma.project.summary.test.check;

import cn.enigma.project.common.Globals;
import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.jpa.entity.BaseEntity;
import cn.enigma.project.jpa.part.PartQuery;
import cn.enigma.project.summary.cache.Task;
import cn.enigma.project.summary.cache.TaskCache;
import cn.enigma.project.summary.cache.pojo.CacheResult;
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

    private static final String CACHE_SEPARATOR = ":";
    private static final String QUERY_TASK_NAME_PREFIX = "queryBy";
    private static final String ADD_TASK_NAME_PREFIX = "addEntity";

    private static final Function<Exception, Exception> EXCEPTION_FUNCTION = (exception) -> new GlobalException(Globals.getOriginException(exception).getMessage());


    /**
     * 请求req添加到数据库工具方法
     *
     * @param taskCache         任务缓存
     * @param operationName     操作名称
     * @param request           接口请求数据
     * @param entityManager     em
     * @param checkOverFunction 检查结束执行方法
     * @param addFunction       添加方法
     * @param entityClass       数据库实体
     * @param <T>               返回实体泛型
     * @param <R>               请求类泛型
     * @return 数据库实体
     * @throws Exception exception
     */
    public static <T extends BaseEntity, R extends CheckRequest> T addEntity(TaskCache<T> taskCache, String operationName, R request, EntityManager entityManager,
                                                                             CheckOverFunction<T> checkOverFunction, Function<R, T> addFunction, Class<T> entityClass) throws Exception {
        final List<CheckBean> pendingCheckList = getCheckInfo(request);
        final int len = pendingCheckList.size();
        final String[] keys = new String[len];
        // for循环依次执行检查是否重复任务
        for (int i = 0; i < len; i++) {
            CheckBean checkBean = pendingCheckList.get(i);
            // 生成查询任务key，加上前缀防止多个属性有相同的值造成缓存污染
            final String key = operationName + CACHE_SEPARATOR + checkBean.getAttributeName() +
                    CACHE_SEPARATOR + checkBean.getValue();
            // 这里需要将查询任务key保存起来用来在执行添加方法的时候将查询的任务替换成添加的任务
            keys[i] = key;
            // 构造查询代码callable
            Callable<T> queryTaskCallable = () -> queryCall(entityManager, checkBean.getAttributeName(), checkBean.getValue(), entityClass);
            // 生成查询任务，查询任务级别比较低，如果某个数据已经存在查询任务则会直接等待执行结果，避免重复查询
            Task<T> queryTask = taskComplete(QUERY_TASK_NAME_PREFIX + checkBean.getAttributeName(), false, queryTaskCallable);
            // 以具体的数据生成key进行缓存任务，获取任务使用Future.get()一直等待直至返回结果，查询任务暂时设置永不过期
            CacheResult<T> queryTaskResult = taskCache.compute(key, queryTask, Future::get, EXCEPTION_FUNCTION, 0L);
            // 得到查询结果后的执行代码
            checkOverFunction.apply(checkBean, queryTaskResult);
        }
        // 构造添加数据的任务名称
        String addTaskName = ADD_TASK_NAME_PREFIX + CACHE_SEPARATOR + request.getClass().getSimpleName() + CACHE_SEPARATOR + request.hashCode();
        Task<T> addEntityTask = taskComplete(addTaskName, true, () -> addFunction.apply(request));
        // 这里批量添加后，之前的同名查询任务会被addEntityTask任务覆盖，也就是说后续有新的添加请求进来时，先走查询代码，
        // 会发现某个实体属性的某个值已经存在了添加任务，然后直接等待操作结果就行，这样就避免了添加重复数据
        CacheResult<T> addTaskResult = taskCache.batchCompute(addEntityTask, Future::get, EXCEPTION_FUNCTION, 1000L, keys);
        return addTaskResult.result();
    }

    /**
     * 通过反射和属性注解，得到待检测的属性信息
     *
     * @param request 请求类，需要检测的属性用@CheckRepeat注解标注
     * @return 待检验的信息列表
     */
    private static List<CheckBean> getCheckInfo(CheckRequest request) {
        List<CheckBean> list = new ArrayList<>();
        Field[] fields = request.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(CheckRepeat.class)) {
                try {
                    CheckRepeat checkRepeat = field.getAnnotation(CheckRepeat.class);
                    list.add(new CheckBean(checkRepeat.name(), field.get(request).toString(), checkRepeat.tableColumn()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
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
    private static <T extends BaseEntity> T queryCall(EntityManager entityManager, String attributeName, String value, Class<T> entity) {
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
}
