package cn.enigma.project.summary.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author luzh
 * Create: 2019/9/9 上午11:20
 * Modified By:
 * Description: @EnableJpaAuditing 设置时间标签生效
 */
@EnableJpaAuditing
@Configuration
public class BaseEntityConfig {
}
