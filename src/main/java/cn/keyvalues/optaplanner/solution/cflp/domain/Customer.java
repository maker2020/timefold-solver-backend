package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(hidden = true)
    protected List<Assign> assignedStations=new ArrayList<>();

    public Customer(long id,long maxDemand,Location location){
        super(id);
        this.location=location;
        this.maxDemand=maxDemand;
    }

    @Hidden
    public boolean isAssigned(){
        return assignedStations.stream()
                .anyMatch(assign->assign.getStation()!=null && assign.getAssignedDemand()!=null);
    }

    @Hidden
    public long getRemainingDemand(){
        long remainingDemand=maxDemand;
        for (Assign assign : assignedStations) {
            // 无效代码:assign.getCustomer()!=null && assign.getCustomer()==this 
            if(assign.getStation()!=null && assign.getAssignedDemand()!=null){
                remainingDemand-=assign.getAssignedDemand();
            }
        }
        return remainingDemand<0?0:remainingDemand;
    }

}
