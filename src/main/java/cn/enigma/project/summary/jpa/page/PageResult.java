package cn.enigma.project.summary.jpa.page;

import lombok.Data;

import java.util.List;

/**
 * @author luzh
 * Create: 2019-06-04 15:09
 * Modified By:
 * Description:
 */
@Data
public class PageResult<T> {
    private Long total;
    private Integer totalPage;
    private List<T> rows;

    public PageResult(long total, Integer totalPage, List<T> rows) {
        this.total = total;
        this.totalPage = totalPage;
        this.rows = rows;
    }
}
