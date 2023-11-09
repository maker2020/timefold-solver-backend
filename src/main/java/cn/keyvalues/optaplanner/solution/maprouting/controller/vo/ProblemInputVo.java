package cn.keyvalues.optaplanner.solution.maprouting.controller.vo;

import java.util.List;

import cn.keyvalues.optaplanner.common.vo.PlannerInputVo;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Visitor;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "线路规划的问题输入对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProblemInputVo extends PlannerInputVo {

    @Schema(description = "某次求解的问题命名")
    private String problemName;
    
    @Schema(description = "选点（客户点）的列表 ：注意不包含起点",requiredMode = RequiredMode.REQUIRED)
    private List<Customer> customers;

    @Schema(description = "访问者列表（含起点属性）")
    private List<Visitor> visitors;

    @Schema(description = "计算时间限制(s)")
    private Long timeLimit;

}
