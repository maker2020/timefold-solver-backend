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
        solution.put("score", score.toString());
        solution.put("assigns", releaseAssign(assigns));
        return solution;
    }

    public static Map<String,Object> releaseAssign(Assign assign){
        Map<String,Object> assignObj=new HashMap<>();
        assignObj.put("assignedDemand", assign.getAssignedDemand());
        assignObj.put("customer", releaseCustomer(assign.getCustomer()));
        assignObj.put("serverStation", releaseStation(assign.getStation()));
        assignObj.put("betweenDistance", assign.getBetweenDistance());
        return assignObj;
    }

    public static List<Map<String,Object>> releaseAssign(List<Assign> assigns){
        List<Map<String,Object>> assignList=new ArrayList<>();
        assigns.forEach(a->{
            Map<String,Object> assign=releaseAssign(a);
            assignList.add(assign);
        });
        return assignList;
    }

    public static List<Map<String,Object>> releaseCustomers(List<Customer> customers){
        List<Map<String,Object>> list=new ArrayList<>();
        customers.forEach(c->{
            Map<String,Object> releaseCustomer = releaseCustomer(c);
            list.add(releaseCustomer);
        });
        return list;
    }

    public static List<Map<String,Object>> releaseStations(List<ServerStation> stations){
        List<Map<String,Object>> list=new ArrayList<>();
        stations.forEach(s->{
            Map<String,Object> releaseStation = releaseStation(s);
            list.add(releaseStation);
        });
        return list;
    }

    public static Map<String,Object> releaseCustomer(Customer customer){
        if(customer==null) return null;
        Map<String,Object> c=new HashMap<>();
        c.put("remainingDemand", customer.getRemainingDemand());
        c.put("maxDemand", customer.getMaxDemand());
        c.put("location", customer.getLocation());
        c.put("id", customer.getId());
        c.put("demandLevel", customer.getDemandLevel());
        // c.put(null, customer.get)
        return c;
    }

    public static Map<String,Object> releaseStation(ServerStation station){
        if(station==null) return null;
        Map<String,Object> s=new HashMap<>();
        s.put("usedCapacity", station.getUsedCapacity());
        s.put("maxCapacity", station.getMaxCapacity());
        s.put("location", station.getLocation());
        s.put("radius", station.getRadius());
        s.put("id", station.getId());
        s.put("color", station.getColor());
        s.put("demandLevel", station.getDemandLevel());
        // s.put("", station.get)
        return s;
    }

}
