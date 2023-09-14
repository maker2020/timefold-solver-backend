package com.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import com.keyvalues.optaplanner.maprouting.controller.MapRoutingController;
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
    private final BaiduDirection baiduDirection;

    public SolverServiceImpl(SolverManager<MapRoutingSolution, Long> solverManager, SolverFactory<MapRoutingSolution> solverFactory,SolverConfig solverConfig,BaiduDirection baiduDirection) {
        this.solverManager = solverManager;
        this.solverFactory = solverFactory;
        this.solverConfig = solverConfig;
        this.baiduDirection = baiduDirection;
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
            routing.add(new RoutingEntity(id++,points.get(i)));
        }
        solution.setRouting(routing);

        log.info("problem已构建,正在初始化辅助数据...");
        List<List<Point>> p2pList = combinePoint(points, 2);
        Map<String,Integer> distanceMap = createDistanceMap(p2pList);
        MapRoutingController.p2pDistanceMap.putAll(distanceMap);
        
        // 不共享、用户定义部分配置
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(pointInputVo.getTimeLimit()));
        SolverFactory<MapRoutingSolution> factory = SolverFactory.create(solverConfig);
        Solver<MapRoutingSolution> solver = factory.buildSolver();
        MapRoutingSolution result = solver.solve(solution);
        return result;
    }

    private List<List<Point>> combinePoint(List<Point> points,int choose) {
        List<List<Point>> combinationList = new ArrayList<>();
        class Combine{
            static void dfs(List<Point> elements, int choose, int start, List<Point> current, List<List<Point>> combinationCollection) {
                if (choose == 0) {
                    combinationCollection.add(new ArrayList<>(current));
                    return;
                }
                for (int i = start; i < elements.size(); i++) {
                    current.add(elements.get(i));
                    dfs(elements, choose - 1, i + 1, current, combinationCollection);
                    current.remove(current.size() - 1);
                }
            }
        }
        Combine.dfs(points, choose, 0, new ArrayList<>(), combinationList);
        return combinationList;
    }

    /**
     * baiduapi获取两点最短规划距离
     */
    private int calculateDistance(Point point1,Point point2){
        int distance=0;
        String params="""
            {
                'tactics':2
            }
            """;
        Result<JSONObject> result=baiduDirection.direction(point1.longitude, point1.latitude, point2.longitude, point2.latitude, params);
        distance=result.getData().getJSONArray("routes").getJSONObject(0).getIntValue("distance");
        return distance;
    }

    private Map<String,Integer> createDistanceMap(List<List<Point>> p2pList){
        Map<String,Integer> p2pDistanceMap=new HashMap<>();
        for (List<Point> point : p2pList) {
            Point a=point.get(0);
            Point b=point.get(1);
            // a->b
            int distance0 = calculateDistance(a, b);
            p2pDistanceMap.put(a.toString()+"->"+b.toString(), distance0);
            // b->a
            int distance1 = calculateDistance(b, a);
            p2pDistanceMap.put(b.toString()+"->"+a.toString(), distance1);
        }
        return p2pDistanceMap;
    }
    
}
