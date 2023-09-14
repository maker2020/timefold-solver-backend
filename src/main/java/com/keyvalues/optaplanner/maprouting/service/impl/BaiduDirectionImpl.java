package com.keyvalues.optaplanner.maprouting.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import com.keyvalues.optaplanner.maprouting.utils.BaiduMapUtil;

@Service
public class BaiduDirectionImpl implements BaiduDirection{

    @Autowired
    private BaiduMapUtil baiduMap;

    @Override
    public Result<JSONObject> direction(Double origin_lng, Double origin_lat, Double destination_lng,
            Double destination_lat, String params) {
        Point originPoint = new Point(origin_lng, origin_lat);
        Point destinationPoint = new Point(destination_lng, destination_lat);
        JSONObject res;
        if (StringUtils.hasText(params)) {
            res = baiduMap.direction(originPoint, destinationPoint, JSONObject.parseObject(params));
        } else {
            res = baiduMap.direction(originPoint, destinationPoint);
        }
        if (res == null) {
            return Result.failed("路径规划接口出错");
        }
        return Result.OK(res.getJSONObject("result"));
    }
    
}
