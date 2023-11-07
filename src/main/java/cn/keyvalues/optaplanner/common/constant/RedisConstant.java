package cn.keyvalues.optaplanner.common.constant;

public interface RedisConstant {
    
    /**
     * 点与点之间路径最优值关系的key
     */
    String p2pOptimalValueMap="p2pOptimalValueMap";

    /**
     * 问题对应轮询的时间，用于辅助资源释放
     */
    String problemPollTimeMap="problemPollTimeMap";

}
