package cn.keyvalues.optaplanner.solution.cflp.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.keyvalues.optaplanner.common.SolverService;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

public interface CFLPService extends SolverService<ProblemInputVo>{

    /**
     * 实时删除一个服务站
     * @param station
     */
    void deleteStationRealTime(UUID problemID,long stationID) throws RuntimeException;

    /**
     * 实时删除一个客户
     * @param problemID
     * @param customerID
     */
    void deleteCustomerRealTime(UUID problemID,long customerID) throws RuntimeException;

    /**
     * 实时添加一个服务站
     * @param problemID
     * @param newStation
     * @throws RuntimeException
     */
    void addStationRealTime(UUID problemID,ServerStation newStation) throws RuntimeException;

    /**
     * 实时添加一个客户
     * @param problemID
     * @param newCustomer
     * @throws RuntimeException
     */
    void addCustomerRealTime(UUID problemID,Customer newCustomer) throws RuntimeException;

    /**************** CURD ****************/
    
    List<Map<String,Object>> listProblem();

}
