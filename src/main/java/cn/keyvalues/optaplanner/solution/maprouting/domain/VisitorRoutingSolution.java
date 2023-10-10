package cn.keyvalues.optaplanner.solution.maprouting.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PlanningSolution
@Getter
@Setter
@ToString
public class VisitorRoutingSolution extends AbstractPersistable{    

    // @ProblemFactCollectionProperty
    // protected List<Location> locationList;

    // @ProblemFactCollectionProperty
    // protected List<VisitorBase> visitorBases;

    @PlanningEntityCollectionProperty
    protected List<Visitor> visitorList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "customerList")
    protected List<Customer> customerList;

    @PlanningScore
    protected HardSoftLongScore score;

    public VisitorRoutingSolution(){
        this(0);
    }

    public VisitorRoutingSolution(long id){
        super(id);
    }
    
}
