package com.keyvalues.optaplanner.maprouting.service;

import com.keyvalues.optaplanner.maprouting.controller.vo.PointInputVo;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;

public interface SolverService {
    MapRoutingSolution mapRoutingSolve(PointInputVo pointInputVo);
}
