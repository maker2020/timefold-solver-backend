package cn.keyvalues.optaplanner.solution.cflp.controller.vo;

import java.util.List;

import cn.keyvalues.optaplanner.common.vo.PlannerInputVo;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "设施位置问题的输入对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProblemInputVo extends PlannerInputVo{

    @Schema(description = "客户列表")
    private List<Customer> customers;

    @Schema(description = "服务站列表")
    private List<ServerStation> serverStations;
    
}
