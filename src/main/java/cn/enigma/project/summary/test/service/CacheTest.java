package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.Globals;
import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.cache.Task;
import cn.enigma.project.summary.cache.TaskCache;
import cn.enigma.project.summary.cache.impl.DefaultTaskCache;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;

@Service
public class CacheTest {

    private TaskCache<TestEntity> nameCache = new DefaultTaskCache<>();

    private final Function<Exception, Exception> exceptionConverter = (exception) -> new GlobalException(Globals.getOriginException(exception).getMessage());

    private final TestRepository testRepository;

    @Autowired
    public CacheTest(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity add(String name) throws Exception {
        // 先构造一个查询name是否存在的任务，任务名称为queryByName，然后不可覆盖其他相同任务名称的任务，具体执行操作为使用name查询数据库
        Task<TestEntity> queryByNameTask = taskComplete("queryByName", false, () -> testRepository.findByName(name).orElse(null));
        // 已name为key创建任务缓存，获取返回值使用future.get()方法，一直等待直到数据库返回结果，异常转换用通用的异常转换，该任务永不过期
        CacheResult<TestEntity> nameCacheResult = nameCache.compute(name, queryByNameTask, Future::get, exceptionConverter, 100L);
        // 如果存在name，直接返回实体
        if (nameCacheResult.hasResult()) {
            return nameCacheResult.result();
        }
        // 如果在查询任务中出现异常直接从接口抛出
        nameCacheResult.throwException();
        // name唯一校验通过，开始进行添加数据库，这里还是按照name为key创建缓存任务，具体的执行任务为保存数据库，可覆盖其他任务，1s后缓存失效
        Task<TestEntity> addByNameTask = taskComplete("addByName", true, () -> save(name));
        return nameCache.compute(name, addByNameTask, Future::get, (exception) -> new GlobalException("添加失败"), 1000L).result();
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
    private <T> Task<T> taskComplete(String name, boolean coverOthers, Callable<T> callable) {
        return new Task<>(name, coverOthers, callable);
    }

    private TestEntity save(String name) {
        Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
        TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                "four-" + id, "five-" + id, "six-" + id);
        testEntity.setName(name);
        return testRepository.save(testEntity);
    }
}
