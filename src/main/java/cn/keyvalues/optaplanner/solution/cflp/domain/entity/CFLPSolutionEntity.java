package cn.keyvalues.optaplanner.solution.cflp.domain.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import cn.keyvalues.optaplanner.mybatis.handler.cflp.AssignListTypeHandler;
import cn.keyvalues.optaplanner.mybatis.handler.cflp.CustomerListTypeHandler;
import cn.keyvalues.optaplanner.mybatis.handler.cflp.ServerStationListTypeHandler;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import lombok.Data;

@Data
@TableName(value = "facility_location_solution",autoResultMap = true)
public class CFLPSolutionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String problemName;

    @TableField(typeHandler = CustomerListTypeHandler.class)
    private List<Customer> customers;
    @TableField(typeHandler = ServerStationListTypeHandler.class)
    private List<ServerStation> serverStations;
    @TableField(typeHandler = AssignListTypeHandler.class)
    private List<Assign> assigns;

    private Long timeLimit;
    private String score;
    private String problemId;
    private String status;

}
