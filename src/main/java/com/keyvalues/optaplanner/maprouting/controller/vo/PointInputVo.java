package com.keyvalues.optaplanner.maprouting.controller.vo;

import java.util.List;

import com.keyvalues.optaplanner.geo.Point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "线路规划的输入对象")
@Data
public class PointInputVo {
    
    @Schema(description = "选点的列表")
    private List<Point> points;

    @Schema(description = "计算时间限制(s)")
    private Long timeLimit;

    @Schema(description = "是否后台计算(默认true)")
    private Boolean isBackground;
}
