package cn.keyvalues.optaplanner.solution.test.solver;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import cn.keyvalues.optaplanner.solution.test.domain.Customer;
import cn.keyvalues.optaplanner.solution.test.domain.FacilityLocationConstraintConfig;

public class FacilityLocationConstraint implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            serverStationCapicity(constraintFactory),
            serverRadius(constraintFactory)
        };
    }

    /**
    * 容量约束
    */
    Constraint serverStationCapicity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Customer.class)
                .groupBy(Customer::getStation, ConstraintCollectors.sumLong(Customer::getMaxDemand))
                .filter((station, demand) -> demand > station.getMaxCapacity())
                .penalizeConfigurableLong((station, demand) -> demand - station.getMaxCapacity())
                .asConstraint(FacilityLocationConstraintConfig.FACILITY_CAPACITY);
    }

    /**
     * 半径约束
     */
    Constraint serverRadius(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Customer.class)
                .filter(customer->customer.getStation().getRadius()<customer.getDistanceToServerStation())
                .penalizeConfigurableLong(customer->(long)(customer.getDistanceToServerStation()-customer.getStation().getRadius()))
                .asConstraint(FacilityLocationConstraintConfig.SERVER_RADIUS);
    }
    
}
