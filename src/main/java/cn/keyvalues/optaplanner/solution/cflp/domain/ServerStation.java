package cn.keyvalues.optaplanner.solution.cflp.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import cn.keyvalues.optaplanner.solution.cflp.solver.shadow.StationUsedCapacityListener;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "服务站类")
public class ServerStation extends AbstractPersistable {
    
    @Schema(description = "位置")
    protected Location location;
    @Schema(description = "最大容量")
    protected long maxCapacity;
    @Schema(description = "半径")
    protected double radius;

    /**
     * 服务站等级，对应客户需求等级。服务站可服务的需求类型是向下兼容。
     */
    @Schema(description = "可处理的需求等级")
    protected int demandLevel;

    @Schema(description = "前端标记服务站颜色")
    protected String color;
    
    /**
     * 求解器可以对此增删改
     */
    @InverseRelationShadowVariable(sourceVariableName = "station")
    @Schema(description = "分配的客户",hidden = true)
    protected List<Assign> assignedCustomers=new ArrayList<>();

    // @DeepPlanningClone
    // @ShadowVariable(sourceEntityClass = ServerStation.class,sourceVariableName = "assignedCustomers",variableListenerClass = StationUsedCapacityListener.class)
    // @PiggybackShadowVariable(shadowEntityClass = Customer.class,shadowVariableName = "remainingDemand")
    @Schema(description = "已使用容量",hidden = true)
    @ShadowVariable(sourceEntityClass = Customer.class,sourceVariableName = "remainingDemand",variableListenerClass = StationUsedCapacityListener.class)
    protected Long usedCapacity;

    public ServerStation(long id,Location location,long maxCapacity,double radius,int demandLevel){
        super(id);
        this.location=location;
        this.maxCapacity=maxCapacity;
        this.radius=radius;
        this.usedCapacity=0L;
        this.demandLevel=demandLevel;
    }

    @Hidden
    public boolean isUsed() {
        return assignedCustomers.stream()
                .anyMatch(assign->assign.getCustomer()!=null && assign.getAssignedDemand()!=null);
    }

}
