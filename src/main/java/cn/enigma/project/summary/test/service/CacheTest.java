package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.Globals;
import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.cache.CacheService;
import cn.enigma.project.summary.cache.impl.MemoryCacheImpl;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

@Service
public class CacheTest {

    private CacheService<TestEntity> nameCache = new MemoryCacheImpl<>(0L);
    private CacheService<TestEntity> entityCache = new MemoryCacheImpl<>(0L);

    private final Function<Exception, Exception> exceptionConverter = (exception) -> new GlobalException(Globals.getOriginException(exception).getMessage());

    private final TestRepository testRepository;

    @Autowired
    public CacheTest(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity add(String name) throws Exception {
        CacheResult<TestEntity> nameCacheResult = nameCache.compute(name, taskComplete(() -> testRepository.findByName(name).orElse(null)), Future::get, exceptionConverter, 1000L);
        if (nameCacheResult.hasResult()) {
            return nameCacheResult.result();
        }
        nameCacheResult.throwException();
        return entityCache.compute(name, taskComplete(() -> save(name)), Future::get, (exception) -> new GlobalException("添加失败"), 1000L, () -> nameCache.removeCache(name, () -> {})).result();
    }

    private <T> FutureTask<T> taskComplete(Callable<T> callable) {
        return new FutureTask<>(callable);
    }

    private TestEntity save(String name) {
        Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
        TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                "four-" + id, "five-" + id, "six-" + id);
        testEntity.setName(name);
        return testRepository.save(testEntity);
    }
}
