package cn.keyvalues.optaplanner.solution.test.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PlanningSolution
@Getter
@Setter
@ToString
public class FacilityLocationSolution extends AbstractPersistable{
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "serverStationList")
    protected List<ServerStation> serverStations;

    @PlanningEntityCollectionProperty
    protected List<Customer> customers;

    @PlanningScore
    protected HardSoftLongScore score;

    @ConstraintConfigurationProvider
    private FacilityLocationConstraintConfig constraintConfiguration = new FacilityLocationConstraintConfig();

    public FacilityLocationSolution(){
        this(0);
    }

    public FacilityLocationSolution(long id){
        super(id);
    }

}
