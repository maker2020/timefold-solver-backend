package cn.keyvalues.optaplanner.solution.test.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.test.solver.RemainingCapacityListener;
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

    @InverseRelationShadowVariable(sourceVariableName = "station")
    protected List<Customer> assignedCustomers=new ArrayList<>();

    // 这里应该将sourceVar指向assigned...影子变量，以保证顺序先后。
    @ShadowVariable(variableListenerClass = RemainingCapacityListener.class
            ,sourceEntityClass = ServerStation.class,sourceVariableName = "assignedCustomers")
    protected Long remainingCapacity;

    public ServerStation(long id,Location location,long maxCapacity,double radius){
        super(id);
        this.location=location;
        this.maxCapacity=maxCapacity;
        remainingCapacity=maxCapacity;
        this.radius=radius;
    }

}
