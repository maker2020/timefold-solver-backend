package cn.keyvalues.optaplanner.common;

import java.util.Map;

/**
 * 针对解决方案循环引用解构
 */
public interface CircularRefRelease {
 
    /**
     * 返回无循环引用的solution Map对象
     * @param solution
     * @return
     */
    Map<String,Object> releaseCircular();
    
}
