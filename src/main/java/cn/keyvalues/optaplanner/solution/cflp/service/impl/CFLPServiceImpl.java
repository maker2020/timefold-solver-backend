package cn.keyvalues.optaplanner.solution.cflp.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.solution.cflp.Main;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import cn.keyvalues.optaplanner.solution.cflp.service.CFLPService;
import cn.keyvalues.optaplanner.utils.SolutionHelper;

@Service
public class CFLPServiceImpl implements CFLPService{

    private final SolverConfig solverConfig;
    private SolutionHelper solutionHelper;

    public CFLPServiceImpl(@Qualifier("cflpConfig") SolverConfig solverConfig,SolutionHelper solutionHelper) {
        this.solverConfig = solverConfig;
        this.solutionHelper=solutionHelper;
    }

    @Override
    public Result<?> solveAsync(ProblemInputVo problemInputVo) {
        FacilityLocationSolution initializedSolution = generateSolution(problemInputVo);
        Main.test(initializedSolution);
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(problemInputVo.getTimeLimit()));
        // 随机问题ID，用于跟踪问题
        UUID problemID=UUID.randomUUID();
        solutionHelper.solveAsync(initializedSolution, solverConfig, problemID, update->{
            // 求解记录放入队列前的处理
        }, update->{
            // 持久化存储的更新...
        }, lastSolution->{
            // 持久化存储的更新...
        });
        Map<String,Object> data=new HashMap<>();
        data.put("problemID", problemID.toString());
        return Result.OK("请求成功！正在后台处理...", data);
    }

    @Override
    public Map<String, Object> pollUpdate(UUID problemID, long intervalTime) throws Exception {
        return solutionHelper.pollUpdate(problemID, intervalTime);
    }

    private FacilityLocationSolution generateSolution(ProblemInputVo problemInputVo){
        FacilityLocationSolution solution=new FacilityLocationSolution();
        List<Customer> customers = problemInputVo.getCustomers();
        long id=0;
        for(Customer c:customers){
            c.setId(id++);
            c.setRemainingDemand(c.getMaxDemand());
        }
        solution.setCustomers(customers);

        List<ServerStation> serverStations = problemInputVo.getServerStations();
        id=0;
        for(ServerStation station:serverStations){
            station.setId(id++);
            station.setUsedCapacity(0L);
        }
        solution.setServerStations(serverStations);

        long demand=Collections.max(customers, (r1,r2)->Long.compare(r1.getMaxDemand(), r2.getMaxDemand())).getMaxDemand();
        List<Long> demandChoices=new ArrayList<>();
        for(int i=1;i<=demand;i++){ // 不能分配0个，因为属于不分配
            demandChoices.add((long)i);
        }
        solution.setDemandChoices(demandChoices);

        // 结果未知只能按最大区限，所以规划变量nullable=true
        // 即 m * n
        List<Assign> assigns=new ArrayList<>();
        for (int i = 0; i < customers.size()*serverStations.size(); i++) {
            Assign assign = new Assign(i);
            // Initialize other properties of Assign if needed
            assigns.add(assign);
        }
        solution.setAssigns(assigns);

        return solution;
    }
    
}
