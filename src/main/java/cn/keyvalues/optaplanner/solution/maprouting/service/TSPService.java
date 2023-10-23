package cn.keyvalues.optaplanner.solution.maprouting.service;

import java.util.Map;
import java.util.UUID;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.solution.maprouting.controller.vo.ProblemInputVo;

public interface TSPService {
    
    /**
     * 异步求解问题
     * @return 返回result含问题id
     */
    Result<?> solveAsync(ProblemInputVo problemInputVo);

    /**
     * <p>返回问题通过solveAsync求解过程中某个阶段的结果</p>
     * <p>返回的对象必须用ObjectMapper来序列化，其他JSON不行</p>
     * @param problemID
     * @param intervalTime 轮询间隔时间(单位ms)
     * @return
     */
    Map<String,Object> pollUpdate(UUID problemID,long intervalTime) throws Exception;

}
