package cn.keyvalues.optaplanner.common.vo;

import java.util.List;

import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ConstraintConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PlannerInputVo {

    @Schema(description = "某次求解的问题命名")
    private String problemName;
    
    @Schema(description = "计算时间限制(s)")
    private Long timeLimit;

    @Schema(description = "约束权重/目标函数系数的配置")
    private List<ConstraintConfig> constraintConfig;

    @Schema(description = "选择的约束列表")
    private List<String> selectedConstraints;

}
