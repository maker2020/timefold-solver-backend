package cn.keyvalues.optaplanner.solution.test.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.test.solver.RemainingDemandListener;
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
    
    protected long maxDemand;
    protected Location location;

    @PlanningVariable(valueRangeProviderRefs = "serverStationList")
    protected ServerStation station;

    // 这个和station稍微差异，因为shadow var计算过程中不受顺序影响,不用导向
    @ShadowVariable(variableListenerClass = RemainingDemandListener.class,sourceEntityClass = Customer.class,sourceVariableName = "station")
    protected Long remainingDemand;

    public Customer(long id,long maxDemand,Location location){
        super(id);
        this.location=location;
        this.maxDemand=maxDemand;
        remainingDemand=maxDemand;
    }

    public double getDistanceToServerStation(){
        return location.getDistanceTo(station.getLocation());
    }

}
