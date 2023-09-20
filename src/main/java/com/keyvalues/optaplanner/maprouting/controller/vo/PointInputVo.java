package com.keyvalues.optaplanner.maprouting.controller.vo;

import java.util.List;

import com.keyvalues.optaplanner.common.enums.TacticsEnum;
import com.keyvalues.optaplanner.geo.Point;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Schema(description = "线路规划的输入对象")
@Data
public class PointInputVo {
    
    @Schema(description = "选点的列表",requiredMode = RequiredMode.REQUIRED)
    private List<Point> points;

    @Schema(description = "访问者列表（出发点相同）发派不同地点。若提供访问者，必须规定起点")
    private List<String> visitors;

    @Schema(description = "规定起点")
    private Point start;
    
    @Schema(description = "规定终点")
    private Point end;

    @Schema(description = "计算时间限制(s)")
    private Long timeLimit;

    @Schema(description = "地图API规划策略")
    private TacticsEnum tactics;

}
