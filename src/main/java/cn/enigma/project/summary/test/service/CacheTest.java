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
        Task<TestEntity> queryByNameTask = taskComplete("queryByName", false, 0, () -> testRepository.findByName(name).orElse(null));
        CacheResult<TestEntity> nameCacheResult = nameCache.compute(name, queryByNameTask, Future::get, exceptionConverter, 0L);
        if (nameCacheResult.hasResult()) {
            return nameCacheResult.result();
        }
        nameCacheResult.throwException();
        Task<TestEntity> addByNameTask = taskComplete("addByName", true, 1, () -> save(name));
        return nameCache.compute(name, addByNameTask, Future::get, (exception) -> new GlobalException("添加失败"), 1000L).result();
    }

    private <T> Task<T> taskComplete(String name, boolean coverOthers, int priority, Callable<T> callable) {
        return new Task<>(name, coverOthers, priority, callable);
    }

    private TestEntity save(String name) {
        Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
        TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                "four-" + id, "five-" + id, "six-" + id);
        testEntity.setName(name);
        return testRepository.save(testEntity);
    }
}
