package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.task.CachedTaskCompute;
import cn.enigma.project.summary.task.TaskCompute;
import cn.enigma.project.summary.task.TaskResult;
import cn.enigma.project.summary.test.check.AddCheckUtil;
import cn.enigma.project.summary.test.controller.req.TestReq;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
@Service
public class CacheTest1 {

    @PersistenceContext
    private EntityManager entityManager;

    private TaskCompute<TestEntity> nameCache = new CachedTaskCompute<>();

    private final TestRepository testRepository;

    public CacheTest1(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity add(TestReq testReq) throws Exception {
        return addV1(testReq);
    }

    public TestEntity addV1(TestReq testReq) throws Exception {
        TaskResult<TestEntity> taskResult = AddCheckUtil.addEntityV1(
                "addTestEntity",
                TestEntity.class,
                nameCache,
                testReq,
                entityManager,
                this::save
        );
        log.info("{}", taskResult);
        return taskResult.result();
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
