package cn.enigma.project.summary.common.controller.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author luzh
 * createTime 2017年9月15日 下午7:35:46
 */
@Data
public class TimeRangeReq {
    @ApiModelProperty(value = "开始时间（开始时间）")
    private Long startTime;
    @ApiModelProperty(value = "截止时间（结束时间）")
    private Long endTime;

    public TimeRangeReq(Long startTime, Long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
