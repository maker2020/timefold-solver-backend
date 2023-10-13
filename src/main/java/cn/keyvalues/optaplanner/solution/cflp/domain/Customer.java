package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.cflp.solver.RemainingDemandListener;
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

    @InverseRelationShadowVariable(sourceVariableName = "customer")
    protected List<Assign> assignedStations=new ArrayList<>();

    @ShadowVariable(variableListenerClass = RemainingDemandListener.class,sourceEntityClass = Assign.class,sourceVariableName = "assignedDemand")
    protected Long remainingDemand;

    /**
     * 记录剩余需求量（初始化=demand)
     */
    // @ShadowVariable
    // protected long remainingDemand;

    public Customer(long id,long maxDemand,Location location){
        super(id);
        this.location=location;
        // remainingDemand=demand;
        this.maxDemand=maxDemand;
        remainingDemand=maxDemand;
    }

    public boolean isAssigned(){
        return assignedStations.stream()
                .anyMatch(assign->assign.getCustomer()==this && assign.getStation()!=null);
    }

}
