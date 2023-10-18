package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.List;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

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

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "customerList")
    protected List<Customer> customers;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "demandChoices")
    protected List<Long> demandChoices;

    @PlanningEntityCollectionProperty
    protected List<Assign> assigns;

    @PlanningScore
    protected HardMediumSoftLongScore score;

    @ConstraintConfigurationProvider
    private FacilityLocationConstraintConfig constraintConfiguration = new FacilityLocationConstraintConfig();

    public FacilityLocationSolution(){
        this(0);
    }

    public FacilityLocationSolution(long id){
        super(id);
    }

}
