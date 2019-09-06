package cn.enigma.project.summary.test.service;

import cn.enigma.project.summary.jpa.part.PartQuery;
import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:36
 * Modified By:
 * Description:
 */
@Service
public class TestService {

    private final PartQuery<TestEntity> partQuery = new PartQuery<>();

    @PersistenceContext
    private EntityManager entityManager;

//    @Autowired
//    public TestService(TestRepository testRepository) {
//        this.testRepository = testRepository;
//    }


    public List<TestOneBO> listOne() {
        return partQuery.statisticsQuery(entityManager, TestOneBO.class, TestEntity.class, entityManager.getCriteriaBuilder().and());
    }
}
