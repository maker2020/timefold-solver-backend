package cn.keyvalues.optaplanner.solution.cflp.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "约束权重/目标函数系数的配置")
public class ConstraintConfig{

    String constraintID;

    @Schema(description = "三个可选值:[Hard、Medium、Soft]")
    String constraintLevel;

    Long constraintWeight;

}