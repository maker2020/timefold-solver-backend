package cn.keyvalues.optaplanner.solution.cflp.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.cflp.solver.shadow.DistanceListener;
import lombok.Getter;
import lombok.Setter;

/**
 * 为了表示多对多，即用一对一，不用PlanningListVariable即要写约束来限制重复
 */
@Setter
@Getter
@PlanningEntity
public class Assign extends AbstractPersistable{

    @PlanningVariable(valueRangeProviderRefs = "customerList",nullable = true)
    protected Customer customer;

    @PlanningVariable(valueRangeProviderRefs = "serverStationList",nullable = true)
    protected ServerStation station;

    @PlanningVariable(valueRangeProviderRefs = "demandChoices",nullable = true)
    protected Long assignedDemand;

    @ShadowVariable(sourceVariableName = "customer",variableListenerClass = DistanceListener.class,sourceEntityClass = Assign.class)
    @ShadowVariable(sourceVariableName = "station",variableListenerClass = DistanceListener.class,sourceEntityClass = Assign.class)
    protected Double betweenDistance;

    // 如果两个shadowvar共同监听不行，则在此加一个规划变量

    public Assign(){}

    public Assign(long id){
        super(id);
    }
    
}
