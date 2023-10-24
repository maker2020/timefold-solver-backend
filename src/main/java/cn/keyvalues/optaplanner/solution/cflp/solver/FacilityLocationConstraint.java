package cn.keyvalues.optaplanner.solution.cflp.solver;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationConstraintConfig;

public class FacilityLocationConstraint implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            noRestDemand(constraintFactory),
            serverStationCapicity(constraintFactory),
            uniqueEntity(constraintFactory),
            serverRadius(constraintFactory),
            noOverDemand(constraintFactory),
            greedyDemand(constraintFactory), // 这个和很多因素关联考虑
            lessStation(constraintFactory),
        };
    }

    /**
     * 消耗需求的奖励（触发贪婪）
     */
    Constraint greedyDemand(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Assign.class)
                .rewardConfigurableLong(assign->assign.getAssignedDemand())
                .asConstraint(FacilityLocationConstraintConfig.GREEDY_DEMAND);
    }

    /**
     * 不过度分配：分配量不能 大于 当前剩余需求量
     */
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
    Constraint noRestDemand(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Customer.class)
                .filter(c->c.getRemainingDemand()!=0)
                .penalizeConfigurable()
                .asConstraint(FacilityLocationConstraintConfig.NO_REST_DEMAND);
    }

    /**
     * 服务站最少约束
     */
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
    Constraint serverRadius(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Assign.class)
                .filter(assign->assign.getStation().getRadius()<assign.getBetweenDistance())
                // .penalizeConfigurableLong(assign->(long)(assign.getBetweenDistance()-assign.getStation().getRadius()))
                .penalizeConfigurable()
                .asConstraint(FacilityLocationConstraintConfig.SERVER_RADIUS);
    }
    
}
