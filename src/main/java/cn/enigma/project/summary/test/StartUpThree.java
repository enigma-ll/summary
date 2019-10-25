package cn.enigma.project.summary.test;

import cn.enigma.project.common.startup.StartUpRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

/**
 * @author luzh
 * Create: 2019-10-25 15:26
 * Modified By:
 * Description:
 */
@Slf4j
@Component
@Order(3)
public class StartUpThree extends StartUpRunner {

    public StartUpThree(Executor executor) {
        super(executor);
    }

    public void initThree() {
        try {
            lock.lock();
            log.info("three start up: {}", LocalDateTime.now().toString());
            executor.execute(() -> log.info("thread start up: {}", LocalDateTime.now().toString()));
        } finally {
            lock.unlock();
        }
    }
}
