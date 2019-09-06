package cn.enigma.project.summary.common.controller.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author luzh
 * createTime 2017年9月5日 下午9:26:04
 */
@Data
public class SearchReq {
    @ApiModelProperty(value = "模糊搜索项")
    private String search;
}
