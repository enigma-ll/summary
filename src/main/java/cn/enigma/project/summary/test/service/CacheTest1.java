package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.cache.TaskCache;
import cn.enigma.project.summary.cache.impl.DefaultTaskCache;
import cn.enigma.project.summary.test.check.AddCheckUtil;
import cn.enigma.project.summary.test.check.CheckOverFunction;
import cn.enigma.project.summary.test.controller.req.TestReq;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class CacheTest1 {

    @PersistenceContext
    private EntityManager entityManager;

    private TaskCache<TestEntity> nameCache = new DefaultTaskCache<>();

    private final TestRepository testRepository;

    public CacheTest1(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity add(TestReq testReq) throws Exception {
        // 这里表示每个重复检查项发现重复之后的执行代码
        CheckOverFunction<TestEntity> overFunction = (checkBean, result) -> {
            // 如果发现异常，直接从接口throw
            if (result.hasResult()) {
                throw new GlobalException(checkBean.getName() + "【" + checkBean.getValue() + "】已存在");
            }
            result.throwException();
        };
        return AddCheckUtil.addEntity(nameCache,
                "addTestEntity",
                testReq,
                entityManager,
                overFunction,
                this::save,
                TestEntity.class
        );
    }

    private TestEntity save(TestReq req) {
        Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
        TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                "four-" + id, "five-" + id, "six-" + id);
        testEntity.setName(req.getName());
        testEntity.setAge(req.getAge());
        return testRepository.save(testEntity);
    }
}
