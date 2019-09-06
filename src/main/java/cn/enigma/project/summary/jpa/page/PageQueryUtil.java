package cn.enigma.project.summary.jpa.page;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author luzh
 * Create: 2019-07-24 13:48
 * Modified By:
 * Description:
 */
public class PageQueryUtil<Entity, Result> {

    private final PageQueryRepository<Entity> pageQueryRepository;

    public PageQueryUtil(PageQueryRepository<Entity> pageQueryRepository) {
        this.pageQueryRepository = pageQueryRepository;
    }

    /**
     * 通用的分页查询
     *
     * @param pageQuery 分页
     * @param mapper    数据转换mapper
     * @return 结果
     */
    public PageResult<Result> pageQuery(PageQuery pageQuery, Function<Entity, Result> mapper) {
        return pageQuery(null, pageQuery, mapper);
    }

    /**
     * 通用的多条件、分页查询
     *
     * @param spec      动态查询条件
     * @param pageQuery 分页
     * @param mapper    查询结果转换mapper
     * @return 结果
     */
    public PageResult<Result> pageQuery(Specification<Entity> spec, PageQuery pageQuery, Function<Entity, Result> mapper) {
        if (pageQuery.pageQuery()) {
            PageRequest pageRequest = PageRequest.of(pageQuery.getPage() - 1, pageQuery.getRows());
            Page<Entity> page = findAll(spec, pageRequest);
            return new PageResult<>(
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.getContent().stream().map(mapper).collect(Collectors.toList())
            );
        } else {
            List<Entity> list = findAll(spec);
            return new PageResult<>(
                    list.size(),
                    list.isEmpty() ? 0 : 1,
                    list.stream().map(mapper).collect(Collectors.toList())
            );
        }
    }

    private Page<Entity> findAll(Specification<Entity> spec, Pageable pageable) {
        if (null == spec) {
            return pageQueryRepository.findAll(pageable);
        } else {
            return pageQueryRepository.findAll(spec, pageable);
        }
    }

    private List<Entity> findAll(Specification<Entity> spec) {
        if (null == spec) {
            return pageQueryRepository.findAll();
        } else {
            return pageQueryRepository.findAll(spec);
        }
    }


}
