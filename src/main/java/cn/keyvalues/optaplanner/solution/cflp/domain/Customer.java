package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.cflp.solver.shadow.RemainingDemandListener;
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

    /**
     * 需求等级，对应服务站可服务等级。没确定上限，按越高越好
     */
    protected int demandLevel;

    @InverseRelationShadowVariable(sourceVariableName = "customer")
    @Schema(hidden = true)
    protected List<Assign> assignedStations=new ArrayList<>();

    @ShadowVariable(sourceEntityClass = Customer.class,sourceVariableName = "assignedStations",variableListenerClass = RemainingDemandListener.class)
    @Schema(hidden = true)
    protected Long remainingDemand;

    public Customer(long id,long maxDemand,Location location,int demandLevel){
        super(id);
        this.location=location;
        this.maxDemand=maxDemand;
        this.remainingDemand=maxDemand;
        this.demandLevel=demandLevel;
    }

    @Hidden
    public boolean isAssigned(){
        return assignedStations.stream()
                .anyMatch(assign->assign.getStation()!=null && assign.getAssignedDemand()!=null);
    }

}
