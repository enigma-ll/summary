package cn.enigma.project.summary.test;

import cn.enigma.project.common.startup.SystemInit;
import cn.enigma.project.common.util.SnowflakeIdWorker;
import cn.enigma.project.summary.test.dao.TestRepository;
import cn.enigma.project.summary.test.entity.TestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:48
 * Modified By:
 * Description:
 */
@Slf4j
@Component
public class TestEntityInit extends SystemInit {

    private TestRepository testRepository;
    private final SnowflakeIdWorker snowflakeIdWorker = SnowflakeIdWorker.getInstance(1L, 1L);

    public TestEntityInit(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public void initTestData() {
        for (int i = 0; i < 2; i++) {
            Long id = snowflakeIdWorker.nextId();
            TestEntity testEntity = new TestEntity(id.toString(), "two-" + id, "three-" + id,
                    "four-" + id, "five-" + id, "six-" + id);
//            testRepository.save(testEntity);
        }
        log.info("init finish");
    }
}
