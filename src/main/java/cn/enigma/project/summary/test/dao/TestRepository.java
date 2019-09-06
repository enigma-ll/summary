package cn.enigma.project.summary.test.dao;

import cn.enigma.project.summary.test.entity.TestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author luzh
 * Create: 2019/9/6 上午10:34
 * Modified By:
 * Description:
 */
@Repository
public interface TestRepository extends CrudRepository<TestEntity, Integer> {
}
