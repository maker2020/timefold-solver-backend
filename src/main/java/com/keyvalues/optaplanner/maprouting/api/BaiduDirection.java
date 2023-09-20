package com.keyvalues.optaplanner.maprouting.api;

import com.alibaba.fastjson.JSONObject;
import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.common.enums.TacticsEnum;
import com.keyvalues.optaplanner.geo.Point;

public interface BaiduDirection {
    
    Result<JSONObject> direction(Double origin_lng,Double origin_lat,Double destination_lng,Double destination_lat,String params);
    /**
     * 百度API: 获得两点某策略的最优值
     * @return
     */
    long calculateOptimalValue(Point point1,Point point2,TacticsEnum tactics);

}
