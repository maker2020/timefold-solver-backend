package cn.keyvalues.optaplanner.common;

import java.util.Map;
import java.util.UUID;

/**
 * 求解器接口
 */
public interface SolverService<ProblemInputVo> {
    
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

    /**
     * 终止问题，并直接获取当前结果
     * @param problemID
     * @param save 保存
     * @return
     */
    Map<String,Object> terminalProblem(UUID problemID,boolean save);

}
