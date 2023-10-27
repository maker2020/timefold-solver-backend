package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfigurationProvider;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

import cn.keyvalues.optaplanner.common.CircularRefRelease;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PlanningSolution
@Getter
@Setter
@ToString
public class FacilityLocationSolution extends AbstractPersistable implements CircularRefRelease{
    
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

    @Override
    public Map<String,Object> releaseCircular() {
        Map<String,Object> solution=new HashMap<>();
        List<Map<String,Object>> assignList=new ArrayList<>();
        for (int i = 0; i < assigns.size(); i++) {
            Map<String,Object> assignObj=new HashMap<>();
            Assign assign = assigns.get(i);
            assignObj.put("assignedDemand", assign.getAssignedDemand());
            assignObj.put("customer", releaseCustomer(assign.getCustomer()));
            assignObj.put("serverStation", releaseStation(assign.getStation()));
            assignList.add(assignObj);
        }
        solution.put("assigns", assignList);
        return solution;
    }

    private Map<String,Object> releaseCustomer(Customer customer){
        if(customer==null) return null;
        Map<String,Object> c=new HashMap<>();
        c.put("remainingDemand", customer.getRemainingDemand());
        c.put("maxDemand", customer.getMaxDemand());
        c.put("location", customer.getLocation());
        c.put("id", customer.getId());
        // c.put(null, customer.get)
        return c;
    }

    private Map<String,Object> releaseStation(ServerStation station){
        if(station==null) return null;
        Map<String,Object> s=new HashMap<>();
        s.put("usedCapacity", station.getUsedCapacity());
        s.put("maxCapacity", station.getMaxCapacity());
        s.put("location", station.getLocation());
        s.put("radius", station.getRadius());
        s.put("id", station.getId());
        s.put("color", station.getColor());
        // s.put("", station.get)
        return s;
    }

}
