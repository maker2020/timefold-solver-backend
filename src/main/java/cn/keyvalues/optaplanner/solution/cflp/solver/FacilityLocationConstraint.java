package cn.keyvalues.optaplanner.solution.cflp.solver;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationConstraintConfig;
import io.swagger.v3.oas.annotations.media.Schema;

public class FacilityLocationConstraint implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            noRestDemand(constraintFactory), // 目前这个约束所用的影子变量其listener有一个corruption需等待github issue解决
            serverStationCapicity(constraintFactory),
            uniqueEntity(constraintFactory), // 后期如果多对多关系中，有一方能确定数量就可以改变模型，此约束可去除
            serverRadius(constraintFactory),
            noOverDemand(constraintFactory),
            greedyDemand(constraintFactory), // 这个和很多因素关联考虑
            lessStation(constraintFactory),
            matchLevel(constraintFactory),
            distanceFromFacility(constraintFactory),
        };
    }

    @Schema(description = FacilityLocationConstraintConfig.DISTANCE_FROM_FACILITY)
    Constraint distanceFromFacility(ConstraintFactory constraintFactory){
        return constraintFactory.forEach(Assign.class)
                .penalizeConfigurableLong(assign->assign.getBetweenDistance().longValue())
                .asConstraint(FacilityLocationConstraintConfig.DISTANCE_FROM_FACILITY);
    }
    
    /**
     * 分配需求的等级与服务站等级匹配：服务站必须更高级
     */
    @Schema(description = FacilityLocationConstraintConfig.MATCH_LEVEL)
    Constraint matchLevel(ConstraintFactory constraintFactory){
        return constraintFactory.forEach(Assign.class)
                .filter(assign->assign.getCustomer().getDemandLevel()>assign.getStation().getDemandLevel())
                .penalizeConfigurable()
                .asConstraint(FacilityLocationConstraintConfig.MATCH_LEVEL);
    }

    /**
     * 消耗需求的奖励（触发贪婪）
     */
    @Schema(description = FacilityLocationConstraintConfig.GREEDY_DEMAND)
    Constraint greedyDemand(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Assign.class)
                .rewardConfigurableLong(assign->assign.getAssignedDemand())
                .asConstraint(FacilityLocationConstraintConfig.GREEDY_DEMAND);
    }

    /**
     * 不过度分配：分配量不能 大于 当前剩余需求量
     */
    @Schema(description = FacilityLocationConstraintConfig.NO_OVER_DEMAND)
    Constraint noOverDemand(ConstraintFactory constraintFactory){
        return constraintFactory.forEach(Assign.class)
                .groupBy(Assign::getCustomer,ConstraintCollectors.sumLong(Assign::getAssignedDemand))
                .filter((customer,totalAssign)->totalAssign>customer.getMaxDemand())
                .penalizeConfigurableLong((c,s)->s-c.getMaxDemand())
                .asConstraint(FacilityLocationConstraintConfig.NO_OVER_DEMAND);
    }

    /**
     * 不能有剩余需求
     */
    @Schema(description = FacilityLocationConstraintConfig.NO_REST_DEMAND)
    Constraint noRestDemand(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Customer.class)
                .filter(c->c.getRemainingDemand()!=0)
                .penalizeConfigurable()
                .asConstraint(FacilityLocationConstraintConfig.NO_REST_DEMAND);
    }

    /**
     * 服务站最少约束
     */
    @Schema(description = FacilityLocationConstraintConfig.LESS_STATION)
    Constraint lessStation(ConstraintFactory constraintFactory) {
        // it is automatically filtered to only contain entities
        // for which each genuine PlanningVariable (of the sourceClass or a superclass thereof) has a non-null value
        return constraintFactory.forEach(Assign.class)
                // .filter(assign->assign.getAssignedDemand()!=null 
                //         && assign.getCustomer()!=null && assign.getStation()!=null)
                .groupBy(Assign::getStation)
                .penalizeConfigurableLong(station->1)
                .asConstraint(FacilityLocationConstraintConfig.LESS_STATION);
    }
    
    /**
     * 不重复约束
     */
    @Schema(description = FacilityLocationConstraintConfig.UNIQUE_ENTITY)
    Constraint uniqueEntity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Assign.class)
                .join(Assign.class,Joiners.lessThan(Assign::getId),
                        Joiners.equal(Assign::getCustomer),Joiners.equal(Assign::getStation))
                .penalizeConfigurable()
                .asConstraint(FacilityLocationConstraintConfig.UNIQUE_ENTITY);
    }

    /**
    * 不超出服务站容量约束
    */
    @Schema(description = FacilityLocationConstraintConfig.FACILITY_CAPACITY)
    Constraint serverStationCapicity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Assign.class)
                .groupBy(Assign::getStation, ConstraintCollectors.sumLong(Assign::getAssignedDemand))
                .filter((station, demand) -> demand > station.getMaxCapacity())
                .penalizeConfigurableLong((station, demand) -> demand - station.getMaxCapacity())
                .asConstraint(FacilityLocationConstraintConfig.FACILITY_CAPACITY);
    }

    /**
     * 半径约束
     */
    @Schema(description = FacilityLocationConstraintConfig.SERVER_RADIUS)
    Constraint serverRadius(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Assign.class)
                .filter(assign->assign.getStation().getRadius()<assign.getBetweenDistance())
                // .penalizeConfigurableLong(assign->(long)(assign.getBetweenDistance()-assign.getStation().getRadius()))
                .penalizeConfigurable()
                .asConstraint(FacilityLocationConstraintConfig.SERVER_RADIUS);
    }
    
}
