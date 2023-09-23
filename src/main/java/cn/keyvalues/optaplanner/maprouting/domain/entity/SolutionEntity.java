package cn.keyvalues.optaplanner.maprouting.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName(value = "visitor_routing_solution")
public class SolutionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(hidden = true)
    private Long id;
    private String problemName;
    private String customersJson;
    private String visitorsJson;
    private Long timeLimit;
    private String score;
    private String problemId;
    private String status;
}
