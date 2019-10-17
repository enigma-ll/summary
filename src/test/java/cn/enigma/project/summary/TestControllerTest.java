package cn.enigma.project.summary;

import cn.enigma.project.summary.test.entity.TestEntity;
import cn.enigma.project.summary.test.service.TestService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SummaryApplication.class)
public class TestControllerTest {

    @Autowired
    private TestService testService;

    @Before
    public void setUp() throws Exception {
        // 初始化测试用例类中由Mockito的注解标注的所有模拟对象
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test1() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                System.out.println(1);
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TestEntity testEntity = null;
                try {
//                    testEntity = testService.add("123");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                System.out.println(testEntity);
                finish.countDown();
            });
        }
        countDownLatch.countDown();
        System.out.println("run");
        finish.await();
    }
}
