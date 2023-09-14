package com.keyvalues.optaplanner.maprouting.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;

@Component
public class RoutingUtil {

    public static BaiduDirection baiduDirection;

    @Autowired
    public void setBaiduDirection(BaiduDirection baiduDirection) {
        RoutingUtil.baiduDirection = baiduDirection;
    }
    
    /**
     * baiduapi获取两点最短规划距离
     */
    public static int calculateDistance(RoutingEntity r1,RoutingEntity r2){
        int distance=0;
        Point point1 = r1.getVisitPoint();
        Point point2 = r2.getVisitPoint();
        String params="""
            {
                'tactics':2
            }
            """;
        Result<JSONObject> result=baiduDirection.direction(point1.longitude, point1.latitude, point2.longitude, point2.latitude, params);
        distance=result.getData().getJSONArray("routes").getJSONObject(0).getIntValue("distance");
        return distance;
    }

}
