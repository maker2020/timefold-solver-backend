package cn.keyvalues.optaplanner.solution.cflp.service;

import java.util.UUID;

import cn.keyvalues.optaplanner.common.SolverService;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;

public interface CFLPService extends SolverService<ProblemInputVo>{

    /**
     * 实时删除一个服务站
     * @param station
     */
    void deleteStationRealTime(UUID problemID,long stationID) throws IllegalStateException;

    /**
     * 实时删除一个客户
     * @param problemID
     * @param customerID
     */
    void deleteCustomerRealTime(UUID problemID,long customerID) throws IllegalStateException;

}
