package cn.keyvalues.optaplanner.solution.cflp.controller.vo;

import java.util.List;

import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "设施位置问题的输入对象")
@Data
public class ProblemInputVo {

    @Schema(description = "某次求解的问题命名")
    private String problemName;

    @Schema(description = "客户列表")
    private List<Customer> customers;

    @Schema(description = "服务站列表")
    private List<ServerStation> serverStations;

    @Schema(description = "计算时间限制(s)")
    private Long timeLimit;

}
