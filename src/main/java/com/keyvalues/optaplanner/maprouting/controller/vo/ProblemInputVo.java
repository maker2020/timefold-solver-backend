package com.keyvalues.optaplanner.maprouting.controller.vo;

import java.util.List;

import com.keyvalues.optaplanner.common.enums.TacticsEnum;
import com.keyvalues.optaplanner.maprouting.domain.Location;
import com.keyvalues.optaplanner.maprouting.domain.Visitor;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Schema(description = "线路规划的问题输入对象")
@Data
public class ProblemInputVo {

    @Schema(description = "某次求解的问题命名")
    private String problemName;
    
    @Schema(description = "选点（客户点）的列表 ：注意不包含起点",requiredMode = RequiredMode.REQUIRED)
    private List<Location> locationList;

    @Schema(description = "访问者列表（含起点属性）")
    private List<Visitor> visitors;

    @Schema(description = "计算时间限制(s)")
    private Long timeLimit;

    @Schema(description = "地图API规划策略")
    private TacticsEnum tactics;

}
