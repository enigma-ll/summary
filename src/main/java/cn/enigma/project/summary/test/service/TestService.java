package cn.enigma.project.summary.test.service;

import cn.enigma.project.jpa.part.PartQuery;
import cn.enigma.project.summary.cache.CacheService;
import cn.enigma.project.summary.cache.impl.MemoryCacheImpl;
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
import java.util.concurrent.Future;

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
    private CacheService nameCache = new MemoryCacheImpl<>();
    private CacheService entityCache = new MemoryCacheImpl<>();

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


}
