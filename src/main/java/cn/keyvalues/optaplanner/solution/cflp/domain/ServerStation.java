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
    protected List<Assign> assignedCustomers=new ArrayList<>();

    public ServerStation(long id,Location location,long maxCapacity,double radius){
        super(id);
        this.location=location;
        this.maxCapacity=maxCapacity;
        this.radius=radius;
    }

    public long getUsedCapacity(){
        return assignedCustomers.stream().filter(assign->assign.getStation()==this && assign.getCustomer()!=null)
                .mapToLong(assign->assign.getAssignedDemand()).sum();
    }

    public boolean isUsed() {
        return assignedCustomers.stream()
                .anyMatch(assign->assign.getStation()==this&&assign.getCustomer()!=null);
    }

}
