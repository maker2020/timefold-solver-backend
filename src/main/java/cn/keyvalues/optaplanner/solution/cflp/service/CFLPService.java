package cn.keyvalues.optaplanner.solution.cflp.service;

import java.util.Map;
import java.util.UUID;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;

public interface CFLPService {
    
    /**
     * 异步求解问题
     * @return 返回result含问题id
     */
    Result<?> solveAsync(ProblemInputVo problemInputVo);

    /**
     * @param problemID
     * @param intervalTime 轮询间隔时间(单位ms)
     * @return
     */
    Map<String,Object> pollUpdate(UUID problemID,long intervalTime) throws Exception;

}
