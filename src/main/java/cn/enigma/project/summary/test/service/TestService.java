package cn.enigma.project.summary.test.service;

import cn.enigma.project.common.exception.GlobalException;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.jpa.part.PartQuery;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:36
 * Modified By:
 * Description:
 */
@Slf4j
@Service
public class TestService {

    private final PartQuery<TestEntity> partQuery = new PartQuery<>();

    private ConcurrentHashMap<String, Future<Optional<TestEntity>>> taskMap = new ConcurrentHashMap<>(100);
    private ConcurrentHashMap<String, Future<TestEntity>> entityMap = new ConcurrentHashMap<>(100);

    @PersistenceContext
    private EntityManager entityManager;

    private final TestRepository testRepository;

    @Autowired
    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }


    public List<TestOneBO> listOne() {
        return partQuery.statisticsQuery(entityManager, TestOneBO.class, TestEntity.class, (root, query, criteriaBuilder) -> entityManager.getCriteriaBuilder().and());
    }

    public List<TestEntity> listAll() {
        return (List<TestEntity>) testRepository.findAll();
    }

    public List<TestOneBO> findOne(Integer id) {
        Specification<TestEntity> specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
        return partQuery.statisticsQuery(entityManager, TestOneBO.class, TestEntity.class, specification);
    }

    public TestEntity findEntity(Integer id) {
        return testRepository.findById(id).orElse(null);
    }

    public TestEntity update(Integer id) {
        TestEntity testEntity = testRepository.findById(id).orElseThrow(RuntimeException::new);
        testEntity.setColumnSix(LocalDateTime.now().toString());
        testEntity = testRepository.save(testEntity);
        return testEntity;
    }

    public TestEntity add(String name) throws GlobalException {
        Future<Optional<TestEntity>> future = taskMap.get(name);
        if (future == null) {
            FutureTask<Optional<TestEntity>> futureTask = new FutureTask<>(() -> testRepository.findByName(name));
            future = taskMap.putIfAbsent(name, futureTask);
            if (null == future) {
                futureTask.run();
                future = futureTask;
            }
        }
        try {
            Optional<TestEntity> testEntity = future.get();
            if (testEntity.isPresent()) {
                return testEntity.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Future<TestEntity> entity = entityMap.get(name);
        if (entity == null) {
            FutureTask<TestEntity> futureTask = new FutureTask<>(() -> {
                Long id = SnowflakeIdWorker.getInstance(1L, 1L).nextId();
                TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                        "four-" + id, "five-" + id, "six-" + id);
                testEntity.setName(name);
                return testRepository.save(testEntity);
            });
            entity = entityMap.putIfAbsent(name, futureTask);
            if (null == entity) {
                futureTask.run();
                entity = futureTask;
            }
        }
        try {
            return entity.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new GlobalException("添加失败");
        }
    }
}
