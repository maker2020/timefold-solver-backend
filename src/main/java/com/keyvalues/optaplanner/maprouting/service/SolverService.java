// package com.keyvalues.optaplanner.maprouting.service;

// import java.util.List;
// import java.util.Map;
// import java.util.UUID;

// import com.keyvalues.optaplanner.common.Result;
// import com.keyvalues.optaplanner.maprouting.controller.vo.PointInputVo;
// import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;

// public interface SolverService {

//     @Deprecated
//     MapRoutingSolution mapRoutingSolve(PointInputVo pointInputVo) throws Exception;
    
//     /**
//      * 异步求解（用户必须等待初始化完成后退出才能后台计算，因为初始化结束后才能拿到problemID）
//      * <p>
//      * 当初始化好后，可以退出网页，下次拿problemID调相应接口即可查看结果。
//      * </p>
//      * @return
//      */
//     @Deprecated
//     Result<?> mapRoutingSolveAsync(PointInputVo pointInputVo) throws Exception;

//     @Deprecated
//     Map<String,Object> pollUpdate(UUID problemID) throws Exception;

//     /**
//      * 获取求解问题列表
//      * @return
//      */
//     @Deprecated
//     List<Map<String,Object>> listProblem();

//     /**
//      * 终止及移除问题
//      */
//     @Deprecated
//     void removeProblem(UUID problemID);

//     /**
//      * 终止问题，并直接获取结果
//      * @param problemID
//      * @return
//      */
//     @Deprecated
//     MapRoutingSolution terminalProblem(UUID problemID);
// }
