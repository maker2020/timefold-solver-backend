package com.keyvalues.optaplanner.maprouting.service;

import java.util.Map;
import java.util.UUID;

import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.maprouting.controller.vo.ProblemInputVo;

public interface VisitorRoutingService {
    
    /**
     * 异步求解问题
     * @return 返回result含问题id
     */
    Result<?> solveAsync(ProblemInputVo problemInputVo);

    /**
     * <p>返回问题通过solveAsync求解过程中某个阶段的结果</p>
     * <p>返回的对象必须用ObjectMapper来序列化，其他JSON不行</p>
     * @param problemID
     * @return
     */
    Map<String,Object> pollUpdate(UUID problemID) throws Exception;

}
