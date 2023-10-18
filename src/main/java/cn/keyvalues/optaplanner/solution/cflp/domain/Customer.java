package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

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
    
    protected long maxDemand;
    protected Location location;

    @InverseRelationShadowVariable(sourceVariableName = "customer")
    protected List<Assign> assignedStations=new ArrayList<>();

    public Customer(long id,long maxDemand,Location location){
        super(id);
        this.location=location;
        this.maxDemand=maxDemand;
    }

    public boolean isAssigned(){
        return assignedStations.stream()
                .anyMatch(assign->assign.getCustomer()==this && assign.getStation()!=null);
    }

    public long getRemainingDemand(){
        long remainingDemand=maxDemand;
        for (Assign assgin : assignedStations) {
            if(assgin.getCustomer()==this && assgin.getStation()!=null){
                remainingDemand-=assgin.getAssignedDemand();
            }
        }
        return remainingDemand<0?0:remainingDemand;
    }

}
