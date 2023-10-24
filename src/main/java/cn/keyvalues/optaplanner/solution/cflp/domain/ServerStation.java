package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.cflp.solver.StationUsedCapacityListener;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@PlanningEntity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServerStation extends AbstractPersistable {
    
    protected Location location;
    protected long maxCapacity;
    protected double radius;
    
    /**
     * 求解器可以对此增删改
     */
    @InverseRelationShadowVariable(sourceVariableName = "station")
    @Hidden
    protected List<Assign> assignedCustomers=new ArrayList<>();

    @Hidden
    @ShadowVariable(sourceEntityClass = ServerStation.class,sourceVariableName = "assignedCustomers",variableListenerClass = StationUsedCapacityListener.class)
    protected Long usedCapacity;

    public ServerStation(long id,Location location,long maxCapacity,double radius){
        super(id);
        this.location=location;
        this.maxCapacity=maxCapacity;
        this.radius=radius;
        this.usedCapacity=0L;
    }

    @Hidden
    public boolean isUsed() {
        return assignedCustomers.stream()
                .anyMatch(assign->assign.getCustomer()!=null && assign.getAssignedDemand()!=null);
    }

}
