package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.task.RamCachedTaskCompute;
import cn.enigma.project.summary.task.TaskCompute;
import cn.enigma.project.summary.test.check.CrudCheckDataUtil;
import cn.enigma.project.summary.test.check.CheckResult;
import cn.enigma.project.summary.test.controller.req.TestReq;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
@Service
public class AddCheckTest {

    private static final String OPERATION_ADD = "add";

    @PersistenceContext
    private EntityManager entityManager;

    private TaskCompute<TestEntity> nameCache = new RamCachedTaskCompute<>();

    private final TestRepository testRepository;

    public AddCheckTest(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity add(TestReq testReq) throws Exception {
        CheckResult<TestEntity> checkResult = CrudCheckDataUtil.addEntity(OPERATION_ADD, TestEntity.class, nameCache, testReq,
                entityManager, this::save);
        return CrudCheckDataUtil.getResult(checkResult, testReq);
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
