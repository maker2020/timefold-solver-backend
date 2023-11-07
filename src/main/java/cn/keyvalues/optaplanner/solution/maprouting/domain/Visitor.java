package cn.keyvalues.optaplanner.solution.maprouting.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

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
public class Visitor extends AbstractPersistable implements LocationAware{

    protected VisitorBase base;

    /**
     * 默认不会重复
     */
    @Schema(hidden = true)
    @PlanningListVariable(valueRangeProviderRefs = "customerList")
    protected List<Customer> customers=new ArrayList<>();

    @Schema(hidden = true)
    private List<Map<String,Object>> optimalRelatedMap;

    public Visitor(Long id,VisitorBase base){
        this.id=id;
        this.base=base;
    }

    @Hidden
    @JSONField(serialize = false)
    @Override
    public Location getLocation() {
        return base.getLocation();
    }

    /**
     * 根据现有访问路径，拿到API相关数据
     * @return
     */
    @Hidden
    public void arrangeOptimalRelatedMap(){
        List<Map<String,Object>> list=new ArrayList<>();

        List<Customer> customers = this.getCustomers();
        Point fromPoint;
        for(int i=0;i<customers.size();i++){
            Customer customer = customers.get(i);
            Map<String,Object> map=new HashMap<>();
            StringBuilder sb=new StringBuilder();
            if(i==0){
                fromPoint=this.getBase().getLocation().getPoint();
            }else{
                fromPoint=customers.get(i-1).getLocation().getPoint();
            }
            String key=sb.append(fromPoint.toString())
                    .append("->")
                    .append(customer.getLocation().getPoint().toString())
                    .append(":").append(customer.getLocation().getTactics())
                    .toString();
            Object relatedMap = redisUtil.hget(RedisConstant.p2pOptimalValueMap,key);
            map.put(key, relatedMap);
            list.add(map);
        }
    
        setOptimalRelatedMap(list);
        
    }

}
