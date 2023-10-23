package cn.keyvalues.optaplanner.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("utils")
public class Utils {
    
    public static RedisUtil redisUtil;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil){
        Utils.redisUtil=redisUtil;
    }

}
