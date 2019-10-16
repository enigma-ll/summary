package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.Globals;
import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.cache.CacheService;
import cn.enigma.project.summary.cache.impl.CacheServiceImpl;
import cn.enigma.project.summary.cache.pojo.CacheResult;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Future;

@Service
public class CacheTest {

    private CacheService<Optional<TestEntity>> nameCache = new CacheServiceImpl<>();
    private CacheService<TestEntity> entityCache = new CacheServiceImpl<>();

    private final TestRepository testRepository;

    @Autowired
    public CacheTest(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity add(String name) throws Exception {
        CacheResult<Optional<TestEntity>> nameCacheResult = nameCache.compute(name, () -> testRepository.findByName(name), Future::get, (exception) -> new GlobalException(Globals.getOriginException(exception).getMessage()));
        try {
            if (nameCacheResult.result().isPresent()) {
                return nameCacheResult.result().get();
            }
        } catch (Exception e) {
            throw new GlobalException(Globals.getOriginException(e).getMessage());
        }
        return entityCache.compute(name, () -> save(name), Future::get, (exception) -> new GlobalException("添加失败")).result();
    }

    private TestEntity save(String name) {
        Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
        TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                "four-" + id, "five-" + id, "six-" + id);
        testEntity.setName(name);
        return testRepository.save(testEntity);
    }
}
