package cn.enigma.project.summary.test.controller;

import cn.enigma.project.summary.common.controller.trace.annotation.HttpTrace;
import cn.enigma.project.summary.test.service.TestOneBO;
import cn.enigma.project.summary.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:47
 * Modified By:
 * Description:
 */
@RestController
@RequestMapping("test")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @HttpTrace
    @GetMapping("one")
    public List<TestOneBO> listOne() {
        return testService.listOne();
    }
}
