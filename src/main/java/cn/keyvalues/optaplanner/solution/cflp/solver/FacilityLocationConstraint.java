package cn.keyvalues.optaplanner.solution.cflp.solver;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationConstraintConfig;

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
                .groupBy(Customer::getServerStation, ConstraintCollectors.sumLong(Customer::getDemand))
                .filter((station, demand) -> demand > station.getCapacity())
                .penalizeConfigurableLong((station, demand) -> demand - station.getCapacity())
                .asConstraint(FacilityLocationConstraintConfig.FACILITY_CAPACITY);
    }

    /**
     * 半径约束
     */
    Constraint serverRadius(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Customer.class)
                .filter(customer->customer.getServerStation().getRadius()<customer.getDistanceToServerStation())
                .penalizeConfigurableLong(customer->(long)(customer.getDistanceToServerStation()-customer.getServerStation().getRadius()))
                .asConstraint(FacilityLocationConstraintConfig.SERVER_RADIUS);
    }
    
}
