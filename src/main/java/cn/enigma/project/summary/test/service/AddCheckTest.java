package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.task.TaskRamCacheCompute;
import cn.enigma.project.summary.task.TaskCacheCompute;
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
    private static final String OPERATION_UPDATE = "update";

    @PersistenceContext
    private EntityManager entityManager;

    private TaskCacheCompute<TestEntity> nameCache = new TaskRamCacheCompute<>();

    private final TestRepository testRepository;

    public AddCheckTest(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public TestEntity insert(TestReq testReq) throws Exception {
        CheckResult<TestEntity> checkResult = CrudCheckDataUtil.insertEntity(OPERATION_ADD, TestEntity.class, nameCache, testReq,
                entityManager, this::save);
        return CrudCheckDataUtil.getResult4Insert(checkResult, testReq);
    }

    private TestEntity save(TestReq req, Void v) {
        Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
        TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                "four-" + id, "five-" + id, "six-" + id);
        testEntity.setName(req.getName());
        testEntity.setAge(req.getAge());
        return testRepository.save(testEntity);
    }

    public TestEntity updateTest(Integer id, TestReq testReq) throws Exception {
        CheckResult<TestEntity> checkResult = CrudCheckDataUtil.updateEntity(OPERATION_UPDATE, TestEntity.class, nameCache,
                testReq, id, entityManager, (x, y) -> update(id, testReq));
        log.debug("id {}, req {}, result {}", id, testReq, checkResult);
        return CrudCheckDataUtil.getResult4Update(checkResult, testReq, id);
    }

    private TestEntity update(Integer id, TestReq req) throws GlobalException {
        TestEntity testEntity = testRepository.findById(id).orElseThrow(() -> new GlobalException("xxx"));
        testEntity.setName(req.getName());
        testEntity.setAge(req.getAge());
        return testRepository.save(testEntity);
    }
}
