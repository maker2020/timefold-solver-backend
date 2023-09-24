package cn.keyvalues.optaplanner.maprouting.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.common.enums.TacticsEnum;
import cn.keyvalues.optaplanner.geo.Point;
import cn.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import cn.keyvalues.optaplanner.maprouting.utils.BaiduMapUtil;

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

    @Override
    public long calculateOptimalValue(Point point1, Point point2, TacticsEnum tactics) {
        long optimalValue=0;
        String params=String.format("""
            {
                'tactics':%d
            }
            """,tactics.getValue());
        Result<JSONObject> result=direction(point1.longitude, point1.latitude, point2.longitude, point2.latitude, params);
        // distance=result.getData().getJSONArray("routes").getJSONObject(0).getIntValue("distance");
        JSONObject result_ = result.getData().getJSONArray("routes").getJSONObject(0);
        if(TacticsEnum.TWO.equals(tactics)){
            optimalValue=result_.getIntValue("distance");  
        }else if(TacticsEnum.THIRTEEN.equals(tactics)){
            optimalValue=result_.getIntValue("duration");
        }else if(TacticsEnum.SIX.equals(tactics)){
            optimalValue=result_.getIntValue("taxi_fee");
        }
        return optimalValue;
    }
    
}