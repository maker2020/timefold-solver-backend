package cn.keyvalues.optaplanner.cflp.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 客户（ 需求？ 修车）
 */
@PlanningEntity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Customer extends AbstractPersistable{
    
    protected long demand;
    protected Location location;

    @PlanningVariable(valueRangeProviderRefs = "serverStationList")
    protected ServerStation serverStation;

    // /**
    //  * 记录剩余需求量（初始化=demand)
    //  */
    // @ShadowVariable(variableListenerClass = RemainingDemandShadowListener.class
    //         ,sourceVariableName = "serverStation")
    // protected Long remainingDemand;

    public Customer(long id,long demand,Location location){
        super(id);
        this.demand=demand;
        this.location=location;
        // remainingDemand=demand;
    }

    // @JSONField(serialize = false)
    // @Hidden
    public double getDistanceToServerStation(){
        return location.getDistanceTo(serverStation.getLocation());
    }

    public boolean isAssigned(){
        return serverStation!=null;
    }

}
