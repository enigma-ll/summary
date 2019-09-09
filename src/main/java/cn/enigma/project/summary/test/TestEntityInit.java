package cn.enigma.project.summary.test;

import cn.enigma.project.summary.common.startup.SystemInit;
import cn.enigma.project.summary.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:48
 * Modified By:
 * Description:
 */
@Component
public class TestEntityInit extends SystemInit {

    private TestRepository testRepository;
    private final SnowflakeIdWorker snowflakeIdWorker = SnowflakeIdWorker.getInstance(1L, 1L);

    @Autowired
    public TestEntityInit(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public void initTestData() {
//        for (int i = 0; i < 100; i++) {
            Long id = snowflakeIdWorker.nextId();
            TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                    "four-" + id, "five-" + id, "six-" + id);
            testRepository.save(testEntity);
//        }
    }
}
