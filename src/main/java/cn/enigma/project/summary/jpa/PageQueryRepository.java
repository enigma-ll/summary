package cn.enigma.project.summary.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author luzh
 * Create: 2019-07-24 14:01
 * Modified By:
 * Description:
 */
public interface PageQueryRepository<T> extends JpaSpecificationExecutor<T> {

    Page<T> findAll(Pageable pageable);

    List<T> findAll();
}
