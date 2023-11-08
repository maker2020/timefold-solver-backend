package cn.keyvalues.optaplanner.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("utils")
@Slf4j
public class Utils {
    
    public static RedisUtil redisUtil;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil){
        Utils.redisUtil=redisUtil;
    }

    /**
     * 获取类型属性
     * @param className 类型全限定名
     * @return for example: [int age,String name]
     */
    public static List<Map<String,Object>> childProperties(String className){
        List<Map<String,Object>> list=null;
        try{
            list=new ArrayList<>();
            Class<?> clazz = Class.forName(className);
            Field[] fields = clazz.getDeclaredFields();
            for(Field field:fields){
                Map<String,Object> map=new HashMap<>();
                map.put("name", field.getName());
                map.put("className", field.getType().getName());
                list.add(map);
            }
        }catch(Exception e){
            log.error(e.getMessage());
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println(childProperties("long"));
    }

}
