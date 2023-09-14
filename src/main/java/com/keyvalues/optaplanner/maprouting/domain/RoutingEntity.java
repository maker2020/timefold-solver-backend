package com.keyvalues.optaplanner.maprouting.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.keyvalues.optaplanner.geo.Point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@PlanningEntity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoutingEntity {

    @PlanningId
    private Long id;
    
    /**
     * 规划分配的点
     */
    @PlanningVariable(valueRangeProviderRefs = "pointRange")
    private Point visitPoint;

    /**
     * 规划点的访问顺序
     */
    @PlanningVariable(valueRangeProviderRefs = "orderRange")
    private Integer order;

    public RoutingEntity(Long id){
        this.id=id;
    }

    // 计算两个点之间的距离，使用百度API
    // 调用百度API来计算距离
    // 返回两个点之间的距离
    // public int calculateDistance(RoutingEntity r1,RoutingEntity r2){
    //     System.out.println("RRRRRRRRRRRR:"+r1.toString()+"----------"+r2.toString());
    //     Point p1=r1.getVisitPoint();
    //     Point p2=r2.getVisitPoint();
    //     double latitude = p1.getLatitude();
    //     double longitude = p1.getLongitude();
    //     double latitude2 = p2.getLatitude();
    //     double longitude2 = p2.getLongitude();
    //     double deltaX=longitude2-longitude;
    //     double deltaY=latitude2-latitude;
    //     double distance=Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    //     System.out.println("TTTTTTTTTTTTTTT: "+distance+" /////p1:"+p1.getLatitude()+","+p1.getLongitude()+"/////p2:"+p2.getLatitude()+","+p2.getLongitude());
    //     return (int)(distance*1000);
    // }

    // public int getRoutingDistance(){
    //     double preDistance=getPrevDistance(this);
    //     double nextDistance=getNextDistance(this);
    //     return (int)(preDistance+nextDistance);
    // }

    // public double getNextDistance(RoutingEntity route){
    //     if(route.getNextPoint()==null){
    //         return 0.0;
    //     }
    //     Point next=route.getNextPoint();
    //     return calculateDistance(route.getVisitPoint(), next)+getNextDistance(route);
    // }

    // public double getPrevDistance(RoutingEntity route){
    //     if(route.getPrePoint()==null){
    //         return 0.0;
    //     }
    //     Point prev=route.getPrePoint();
    //     return calculateDistance(route.getVisitPoint(), prev)+getPrevDistance(route);
    // }

}
