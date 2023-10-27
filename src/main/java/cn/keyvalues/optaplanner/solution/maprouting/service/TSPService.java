package cn.keyvalues.optaplanner.solution.maprouting.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.keyvalues.optaplanner.common.SolverService;
import cn.keyvalues.optaplanner.solution.maprouting.controller.vo.ProblemInputVo;

public interface TSPService extends SolverService<ProblemInputVo> {

    /**
     * <p>获取求解问题列表</p>
     * 只包含状态，和问题ID，问题输入相关信息。要获取解需另行访问其他方法
     * @return
     */
    List<Map<String,Object>> listProblem();

    /**
     * 删除问题（数据记录)、并终止问题(若未解决)
     */
    boolean deleteProblem(UUID problemID);

}
