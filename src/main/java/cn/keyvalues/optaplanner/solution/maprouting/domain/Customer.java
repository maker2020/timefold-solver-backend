package cn.keyvalues.optaplanner.solution.maprouting.domain;

import java.util.Map;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;

import com.alibaba.fastjson.annotation.JSONField;

import cn.keyvalues.optaplanner.common.constant.RedisConstant;
import cn.keyvalues.optaplanner.common.geo.Point;
import cn.keyvalues.optaplanner.common.persistence.AbstractPersistable;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static cn.keyvalues.optaplanner.utils.Utils.redisUtil;

@PlanningEntity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Customer extends AbstractPersistable implements LocationAware{
    
    @Schema(description = "位置")
    protected Location location;

    // @Schema(description = "地图API规划策略(上一个点到这里)")
    // protected TacticsEnum tactics=TacticsEnum.TWO;

    // 影子变量：按关联列表左右生成
    @Schema(hidden = true)
    @InverseRelationShadowVariable(sourceVariableName = "customers")
    protected Visitor visitor;
    @Schema(hidden = true)
    @PreviousElementShadowVariable(sourceVariableName = "customers")
    @JSONField(serialize = false)
    protected Customer previousCustomer;
    @Schema(hidden = true)
    @NextElementShadowVariable(sourceVariableName = "customers")
    @JSONField(serialize = false)
    protected Customer nextCustomer;

    public Customer(long id,Location location){
        super(id);
        this.location=location;
    }

    /**
     * 上一个点到这里的距离
     * @return
     */
    @JSONField(serialize = false)
    @Hidden
    @SuppressWarnings("unchecked")
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
            previousPoint = visitor.getLocation().getPoint();
        }else{
            previousPoint = previousCustomer.location.getPoint();
        }
        String key=sb.append(previousPoint.toString())
                .append("->")
                .append(location.getPoint().toString())
                .append(":").append(location.getTactics())
                .toString();
        Object optimalMap = redisUtil.hget(RedisConstant.p2pOptimalValueMap,key);
        return optimalMap==null?0:(long)((Map<String,Object>)optimalMap).get("optimalValue");
        // String key=sb.append(previousPoint.toString()).append("->").append(location.getPoint().toString()).toString();
        // return Main2.p2pOptimalValueMap.getOrDefault(key,0L);
    }

    /**
     * 这里到起点到距离
     * @return
     */
    @Hidden
    @JSONField(serialize = false)
    @SuppressWarnings("unchecked")
    public long getOptimalValueToDepot(){
        StringBuilder sb=new StringBuilder();
        Location baseLocation=visitor.getLocation();
        String key=sb.append(location.getPoint().toString())
                .append("->")
                .append(baseLocation.getPoint().toString())
                .append(":").append(baseLocation.getTactics())
                .toString();
        // return VisitorRoutingController.p2pOptimalValueMap.getOrDefault(key,0L);
        Object optimalMap = redisUtil.hget(RedisConstant.p2pOptimalValueMap,key);
        return optimalMap==null?0:(long)((Map<String,Object>)optimalMap).get("optimalValue");
    }

    /**
     * 这里到目标 的策略优化值(时间？距离？)
     * @return
     */
    @Hidden
    @JSONField(serialize = false)
    @SuppressWarnings("unchecked")
    public long getOptimalValueTo(Location destination){
        StringBuilder sb=new StringBuilder();
        String key=sb.append(location.getPoint().toString())
                .append("->")
                .append(destination.getPoint().toString())
                .append(":").append(destination.getTactics())
                .toString();
        // return VisitorRoutingController.p2pOptimalValueMap.getOrDefault(key,0L);
        Object optimalMap = redisUtil.hget(RedisConstant.p2pOptimalValueMap,key);
        return optimalMap==null?0:(long)((Map<String,Object>)optimalMap).get("optimalValue");
    }

}
