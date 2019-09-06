package cn.enigma.project.summary.jpa.util;

import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author luzh
 * Create: 2019-07-26 11:36
 * Modified By:
 * Description:
 */
public class PredicateUtil {

    /**
     * 组装时间筛选
     *
     * @param root                  Root
     * @param cb                    CriteriaBuilder
     * @param dataTimeAttributeName 时间字段名
     * @param startTime             开始时间（毫秒）
     * @param endTime               结束时间（毫秒）
     * @return Predicate
     */
    public static Optional<Predicate> convertTimeRangeQuery(Root<?> root, CriteriaBuilder cb, @NotNull String dataTimeAttributeName,
                                                            Long startTime, Long endTime) {
        if (StringUtils.isEmpty(dataTimeAttributeName)) {
            return Optional.empty();
        }
        Predicate timePredicate = null;
        if (!StringUtils.isEmpty(startTime)
                && StringUtils.isEmpty(endTime)) {
            timePredicate = cb.gt(root.get(dataTimeAttributeName), startTime);
        } else if (StringUtils.isEmpty(startTime)
                && !StringUtils.isEmpty(endTime)) {
            timePredicate = cb.le(root.get(dataTimeAttributeName), endTime);
        } else if (!StringUtils.isEmpty(startTime)
                && !StringUtils.isEmpty(endTime)) {
            timePredicate = cb.between(root.get(dataTimeAttributeName), startTime,
                    endTime);
        }
        return Optional.ofNullable(timePredicate);
    }

    public static Optional<Predicate> convertSearchQuery(Root<?> root, CriteriaBuilder cb, String searchValue, String... searchAttributeNames) {
        List<Predicate> searchLs = new ArrayList<>();
        if (!StringUtils.isEmpty(searchValue) && searchAttributeNames.length > 0) {
            for (String name : searchAttributeNames) {
                if (!StringUtils.isEmpty(name)) {
                    searchLs.add(cb.like(root.get(name), "%" + searchValue + "%"));
                }
            }
        }
        return searchLs.isEmpty() ? Optional.empty() : Optional.of(cb.or(searchLs.toArray(new Predicate[0])));
    }
}
