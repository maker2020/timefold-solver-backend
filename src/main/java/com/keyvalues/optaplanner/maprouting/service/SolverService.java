package com.keyvalues.optaplanner.maprouting.service;

import java.util.Map;
import java.util.UUID;

import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.maprouting.controller.vo.PointInputVo;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;

public interface SolverService {

    MapRoutingSolution mapRoutingSolve(PointInputVo pointInputVo) throws Exception;
    /**
     * 异步求解
     * @return
     */
    Result<?> mapRoutingSolveAsync(PointInputVo pointInputVo) throws Exception;

    Map<String,Object> pollUpdate(UUID problemID) throws Exception;
}
