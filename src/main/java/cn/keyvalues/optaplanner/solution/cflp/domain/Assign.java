package cn.keyvalues.optaplanner.solution.cflp.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@PlanningEntity
public class Assign extends AbstractPersistable{

    @PlanningVariable(valueRangeProviderRefs = "customerList")
    protected Customer customer;

    @PlanningVariable(valueRangeProviderRefs = "serverStationList")
    protected ServerStation station;

    @PlanningVariable(valueRangeProviderRefs = "demandChoices")
    protected Long assignedDemand;

    // 如果两个shadowvar共同监听不行，则在此加一个规划变量

    public Assign(){}

    public Assign(long id){
        super(id);
    }

    public double getBetweenDistance(){
        return customer.getLocation().getDistanceTo(station.getLocation());
    }
    
}
