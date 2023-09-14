package com.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.stereotype.Service;

import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.controller.vo.PointInputVo;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;
import com.keyvalues.optaplanner.maprouting.service.SolverService;

import lombok.extern.slf4j.Slf4j;

@Service
@SuppressWarnings("unused")
@Slf4j
public class SolverServiceImpl implements SolverService{

    private final SolverManager<MapRoutingSolution, Long> solverManager;
    private final SolverFactory<MapRoutingSolution> solverFactory;
    private final SolverConfig solverConfig;

    public SolverServiceImpl(SolverManager<MapRoutingSolution, Long> solverManager, SolverFactory<MapRoutingSolution> solverFactory,SolverConfig solverConfig) {
        this.solverManager = solverManager;
        this.solverFactory = solverFactory;
        this.solverConfig = solverConfig;
    }

    @Override
    public MapRoutingSolution mapRoutingSolve(PointInputVo pointInputVo) {
        List<Point> points = pointInputVo.getPoints();
        // 构造问题丢给求解器求解。
        MapRoutingSolution solution=new MapRoutingSolution();
        solution.setPointList(points);
        List<Integer> orderRange=new ArrayList<>();
        for(int i=0;i<points.size();i++){
            orderRange.add(i);
        }
        solution.setOrderRange(orderRange);
        List<RoutingEntity> routing=new ArrayList<>();
        long id=0;
        for(int i=0;i<points.size();i++){
            routing.add(new RoutingEntity(id++));
        }
        solution.setRouting(routing);

        log.info("problem已构建,正在求解...");
        // 不共享、用户定义部分配置
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(pointInputVo.getTimeLimit()));
        SolverFactory<MapRoutingSolution> factory = SolverFactory.create(solverConfig);
        Solver<MapRoutingSolution> solver = factory.buildSolver();
        MapRoutingSolution result = solver.solve(solution);
        return result;
    }
    
}
