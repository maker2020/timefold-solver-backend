package com.keyvalues.optaplanner.maprouting.api;

import com.alibaba.fastjson.JSONObject;
import com.keyvalues.optaplanner.common.Result;

public interface BaiduDirection {
    
    Result<JSONObject> direction(Double origin_lng,Double origin_lat,Double destination_lng,Double destination_lat,String params);

}
