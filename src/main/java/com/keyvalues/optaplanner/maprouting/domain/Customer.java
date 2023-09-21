package com.keyvalues.optaplanner.maprouting.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.NextElementShadowVariable;
import org.optaplanner.core.api.domain.variable.PreviousElementShadowVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keyvalues.optaplanner.common.enums.TacticsEnum;
import com.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import com.keyvalues.optaplanner.common.persistence.jackson.JacksonUniqueIdGenerator;
import com.keyvalues.optaplanner.constant.RedisConstant;
import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.service.impl.VisitorRoutingServiceImpl;

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
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Customer extends AbstractPersistable{
    
    @Schema(description = "位置")
    protected Location location;

    @Schema(description = "地图API规划策略(上一个点到这里)")
    protected TacticsEnum tactics=TacticsEnum.TWO;

    // 影子变量：按关联列表左右生成
    @Schema(hidden = true)
    @InverseRelationShadowVariable(sourceVariableName = "customers")
    protected Visitor visitor;
    @Schema(hidden = true)
    @PreviousElementShadowVariable(sourceVariableName = "customers")
    protected Customer previousCustomer;
    @Schema(hidden = true)
    @NextElementShadowVariable(sourceVariableName = "customers")
    protected Customer nextCustomer;

    public Customer(long id,Location location){
        super(id);
        this.location=location;
    }

    /**
     * 上一个点到这里的距离
     * @return
     */
    @JsonIgnore
    public long getOptimalValueFromPreviousStandstill(){
        if(visitor==null){
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        // 有上一个点就直接用上一个点到这里的距离，否则按起点到这里的距离
        // 这个链式模型就解决了前面一个版本的问题
        StringBuilder sb=new StringBuilder();
        Point previousPoint;
        if(previousCustomer==null){
            previousPoint = visitor.getBase().getLocation().getPoint();
        }else{
            previousPoint = previousCustomer.location.getPoint();
        }
        String key=sb.append(previousPoint.toString()).append("->").append(location.getPoint().toString()).append(":").append(tactics).toString();
        Object optimalValue = VisitorRoutingServiceImpl.redisUtil.hget(RedisConstant.p2pOptimalValueMap,key);

        // String key=sb.append(previousPoint.toString()).append("->").append(location.getPoint().toString()).toString();
        // return VisitorRoutingController.p2pOptimalValueMap.getOrDefault(key,0L);
        
        return optimalValue==null?0:(long)optimalValue;
    }

    /**
     * 这里到起点到距离
     * @return
     */
    @JsonIgnore
    public long getOptimalValueToDepot(){
        StringBuilder sb=new StringBuilder();
        Point basePoint=visitor.getBase().getLocation().getPoint();
        String key=sb.append(location.getPoint().toString()).append("->").append(basePoint.toString()).toString();
        // return VisitorRoutingController.p2pOptimalValueMap.getOrDefault(key,0L);
        Object optimalValue=VisitorRoutingServiceImpl.redisUtil.hget(RedisConstant.p2pOptimalValueMap,key);
        return optimalValue==null?0:(long)optimalValue;
    }

}
