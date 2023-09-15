package com.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
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

    private Map<UUID,SolverJob<MapRoutingSolution,Object>> solverJobMap=new ConcurrentHashMap<>();

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
        MapRoutingSolution solution = generateSolution(points);
        
        // 不共享、用户定义部分配置
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(pointInputVo.getTimeLimit()));
        SolverFactory<MapRoutingSolution> factory = SolverFactory.create(solverConfig);
        Solver<MapRoutingSolution> solver = factory.buildSolver();
        MapRoutingSolution result = solver.solve(solution);
        return result;
    }

    @Override
    public Result<?> mapRoutingSolveAsync(PointInputVo pointInputVo) {
        List<Point> points = pointInputVo.getPoints();
        MapRoutingSolution solution = generateSolution(points);
        log.info("problem已构建,正在初始化辅助数据...");
        List<List<Point>> p2pList = combinePoint(points, 2);
        Map<String,Integer> distanceMap = createDistanceMap(p2pList);
        MapRoutingController.p2pDistanceMap.putAll(distanceMap);
        
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(pointInputVo.getTimeLimit()));
        SolverFactory<MapRoutingSolution> factory = SolverFactory.create(solverConfig);
        SolverManager<MapRoutingSolution,Object> solverManager = SolverManager.create(factory, new SolverManagerConfig());
        UUID probleamID = UUID.randomUUID();
        SolverJob<MapRoutingSolution,Object> solverJob = solverManager.solve(probleamID, solution);
        log.info("======================STATUS:"+solverJob.getSolverStatus());
        log.info("======================Duration:"+solverJob.getSolvingDuration());
        solverJobMap.put(probleamID, solverJob);
        Map<String,Object> data=new HashMap<>();
        data.put("problemID", probleamID.toString());
        // debug用的线程
        new Thread(()->{
            for(int i=0;i<10;i++){
                log.info("--------------实时更新状态-----------："+solverJob.getSolverStatus());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    break;
                }
            }
        }).start();
        return Result.OK("请求成功! 正在后台处理...", data);
    }

    /**
     * 构建问题
     * @return
     */
    private MapRoutingSolution generateSolution(List<Point> points){
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
        return solution;
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

    @Override
    public Map<String,Object> pollUpdate(UUID problemID) throws Exception {
        Map<String,Object> updateData=new HashMap<>();
        SolverJob<MapRoutingSolution,Object> solverJob = solverJobMap.get(problemID);
        MapRoutingSolution finalBestSolution = solverJob.getFinalBestSolution();
        List<RoutingEntity> routing = finalBestSolution.getRouting();
        updateData.put("routing", routing);
        updateData.put("status", solverJob.getSolverStatus());
        return updateData;
    }
    
}
