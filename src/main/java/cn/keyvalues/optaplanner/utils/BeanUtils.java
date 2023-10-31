package cn.keyvalues.optaplanner.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BeanUtils {
    
    /**
     * 对象属性转map（包括值为null的字段、继承的字段）
     * @param obj
     * @return
     */
    public static Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                map.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 使用这个应注意两个pojo的property name一致
     * @param from
     * @param to
     */
    public static void copyPropertiesSpring(Object from,Object to){
        org.springframework.beans.BeanUtils.copyProperties(from, to);
    }

}
