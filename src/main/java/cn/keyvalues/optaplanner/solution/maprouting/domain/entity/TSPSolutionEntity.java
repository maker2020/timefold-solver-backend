package cn.keyvalues.optaplanner.solution.maprouting.domain.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import cn.keyvalues.optaplanner.mybatis.handler.maprouting.CustomerListTypeHandler;
import cn.keyvalues.optaplanner.mybatis.handler.maprouting.VisitorListTypeHandler;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Visitor;
import lombok.Data;

@Data
@TableName(value = "visitor_routing_solution",autoResultMap = true)
public class TSPSolutionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String problemName;

    // typehandler处理json序列化
    @TableField(typeHandler = CustomerListTypeHandler.class)
    private List<Customer> customers;
    @TableField(typeHandler = VisitorListTypeHandler.class)
    private List<Visitor> visitors;

    private Long timeLimit;
    private String score;
    private String problemId;
    private String status;
}
