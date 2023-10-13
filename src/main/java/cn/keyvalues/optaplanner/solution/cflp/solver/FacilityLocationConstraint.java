package cn.keyvalues.optaplanner.solution.cflp.solver;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
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
                .penalizeConfigurableLong(assign->(long)(assign.getBetweenDistance()-assign.getStation().getRadius()))
                .asConstraint(FacilityLocationConstraintConfig.SERVER_RADIUS);
    }
    
}
