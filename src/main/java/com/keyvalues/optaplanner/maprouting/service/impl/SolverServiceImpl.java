package com.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
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

    private Map<UUID,SolverJob<MapRoutingSolution,UUID>> solverJobMap;
    private final SolverFactory<MapRoutingSolution> solverFactory;
    private final SolverConfig solverConfig;
    private final BaiduDirection baiduDirection;

    // 这种不能可靠的让浏览器拿到每个情况（取决于时间交点和轮询间隔时差）
    // private Map<UUID,Map<String,Object>> solutionUpdateMap=new ConcurrentHashMap<>();
    // 用等待队列实现
    private Map<UUID,ConcurrentLinkedDeque<Map<String,Object>>> solverSolutionQueue=new ConcurrentHashMap<>();  

    public SolverServiceImpl(SolverManager<MapRoutingSolution, UUID> solverManager, SolverFactory<MapRoutingSolution> solverFactory,SolverConfig solverConfig,BaiduDirection baiduDirection) {
        // 自定义配置，所以不用默认的对象
        // this.solverManager = solverManager;

        // 
        solverJobMap=new ConcurrentHashMap<>();
        this.solverFactory = solverFactory;
        this.solverConfig = solverConfig;
        this.baiduDirection = baiduDirection;
    }

    @Override
    public MapRoutingSolution mapRoutingSolve(PointInputVo pointInputVo) throws Exception{
        List<Point> points = pointInputVo.getPoints();
        // 构造问题丢给求解器求解。
        MapRoutingSolution solution;
        solution = generateSolution(pointInputVo);
        // 不共享、用户定义部分配置
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(pointInputVo.getTimeLimit()));
        SolverFactory<MapRoutingSolution> factory = SolverFactory.create(solverConfig);
        Solver<MapRoutingSolution> solver = factory.buildSolver();
        MapRoutingSolution result = solver.solve(solution);
        return result;
    }

    @Override
    public Result<?> mapRoutingSolveAsync(PointInputVo pointInputVo) throws Exception{
        MapRoutingSolution solution = generateSolution(pointInputVo);
        log.info("problem已构建,正在初始化辅助数据...");
        List<List<Point>> p2pList = combinePoint(pointInputVo.getPoints(), 2);
        Map<String,Integer> distanceMap = createDistanceMap(p2pList);
        MapRoutingController.p2pDistanceMap.putAll(distanceMap);
        
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(pointInputVo.getTimeLimit()));
        SolverFactory<MapRoutingSolution> factory = SolverFactory.create(solverConfig);
        SolverManager<MapRoutingSolution,UUID> solverManager = SolverManager.create(factory, new SolverManagerConfig());
        UUID problemID = UUID.randomUUID();

        // 初始化同步数据的队列
        ConcurrentLinkedDeque<Map<String,Object>> syncQueue = new ConcurrentLinkedDeque<Map<String,Object>>();
        solverSolutionQueue.put(problemID, syncQueue);
        SolverJob<MapRoutingSolution,UUID> solverJob = solverManager.solveAndListen(problemID, (r)->{return solution;},(update)->{
            // 备注：并非同一个对象的引用，不需要深克隆.
            Map<String,Object> newData=new HashMap<>();
            newData.put("status", solverManager.getSolverStatus(problemID));
            newData.put("updatedSolution", update);
            syncQueue.add(newData);
        });
        // 可跟踪问题状态，以及最终解
        solverJobMap.put(problemID, solverJob);
        
        Map<String,Object> data=new HashMap<>();
        data.put("problemID", problemID.toString());
        return Result.OK("请求成功! 正在后台处理...", data);
    }

    @Override
    public Map<String,Object> pollUpdate(UUID problemID) throws Exception {
        Map<String,Object> updateData=new HashMap<>();
        ConcurrentLinkedDeque<Map<String,Object>> updateQueue=solverSolutionQueue.get(problemID);
        // 问题不存在
        if(updateQueue==null) return null;
        Map<String,Object> problemUpdate=updateQueue.poll();
        SolverJob<MapRoutingSolution,UUID> solverJob = solverJobMap.get(problemID);
        if(problemUpdate==null && solverJob.getSolverStatus().equals(SolverStatus.NOT_SOLVING)){
            updateData.put("status", SolverStatus.NOT_SOLVING);

            // 清空该问题对应的资源 (该更新接口已经被浏览器最后一次拿到结果，说明客户端已看到，可以释放)
            solverSolutionQueue.remove(problemID);
            solverJobMap.remove(problemID);
            return updateData;
        }
        // 有status=ACTIVATE，但已没有最新更新数据时，problemUpdate==null
        if(problemUpdate==null && solverJob.getSolverStatus().equals(SolverStatus.SOLVING_ACTIVE)){
            updateData.put("status", SolverStatus.SOLVING_ACTIVE);
            return updateData;
        }
        MapRoutingSolution solution = (MapRoutingSolution)problemUpdate.get("updatedSolution");
        SolverStatus status = (SolverStatus)problemUpdate.get("status");
        List<RoutingEntity> routing = solution.getRouting();
        // 跟某个阶段的解 所绑定的状态为status常量字段
        updateData.put("routing", routing);
        updateData.put("status", status);
        return updateData;
    }

    @Override
    public List<Map<String,Object>> listProblem() {
        List<Map<String,Object>> result=new ArrayList<>();
        Set<UUID> problemIDSet = solverJobMap.keySet();
        for (UUID problemID : problemIDSet) {
            SolverJob<MapRoutingSolution,UUID> solverJob = solverJobMap.get(problemID);
            Map<String,Object> problemData=new HashMap<>();
            SolverStatus solverStatus = solverJob.getSolverStatus();
            // problemData.put("bestRouting", null);
            // if(SolverStatus.NOT_SOLVING.equals(solverStatus)){ // 求解已完成
            //     MapRoutingSolution finalBestSolution;
            //     try {
            //         // 阻塞方法，所以要判断求解完成再来拿，否则为null
            //         finalBestSolution = solverJob.getFinalBestSolution();
            //     } catch (Exception e) {
            //         log.info(e.getMessage());
            //         return null;
            //     }
            //     problemData.put("bestRouting", finalBestSolution.getRouting());
            // }
            problemData.put("status", solverStatus);   
            problemData.put("problemID", problemID);
            // 展示solution信息：包含用户输入的条件、当前最新的一次优化结果
            ConcurrentLinkedDeque<Map<String,Object>> solutionQueue = solverSolutionQueue.get(problemID);
            Map<String,Object> lastSolution = solutionQueue.getLast();
            MapRoutingSolution solution=(MapRoutingSolution)lastSolution.get("updatedSolution");
            problemData.put("solution", solution);
        }
        return result;
    }

    @Override
    public void removeProblem(UUID problemID) {
        if(solverJobMap.containsKey(problemID)){
            SolverJob<MapRoutingSolution,UUID> solverJob = solverJobMap.get(problemID);
            solverJob.terminateEarly();            
        }
        solverJobMap.remove(problemID);
        solverSolutionQueue.remove(problemID);
    }

    @Override
    public MapRoutingSolution terminalProblem(UUID problemID) {
        if(solverJobMap.containsKey(problemID)){
            SolverJob<MapRoutingSolution,UUID> solverJob = solverJobMap.get(problemID);
            // 阻塞到退出
            solverJob.terminateEarly();            
        }
        ConcurrentLinkedDeque<Map<String,Object>> solutionDeque = solverSolutionQueue.get(problemID);
        if(solutionDeque==null || solutionDeque.size()==0) return null;
        Map<String,Object> lastSolution = solutionDeque.getLast();
        MapRoutingSolution solution=(MapRoutingSolution)lastSolution.get("updatedSolution");
        solverJobMap.remove(problemID);
        solverSolutionQueue.remove(problemID);
        return solution;
    }

    /**
     * 构建问题
     * @return
     */
    private MapRoutingSolution generateSolution(PointInputVo pointInputVo) throws Exception{
        List<Point> points = pointInputVo.getPoints();
        if(points==null || points.size()<3){
            throw new Exception("问题构建失败");
        }
        MapRoutingSolution solution=new MapRoutingSolution();
        solution.setPointList(points);
        // 
        List<Integer> orderRange=new ArrayList<>();
        for(int i=0;i<points.size();i++){
            orderRange.add(i);
        }
        solution.setOrderRange(orderRange);
        List<RoutingEntity> routing=new ArrayList<>();
        long id=0;
        for(int i=0;i<points.size();i++){
            Point point = points.get(i);
            RoutingEntity entity = new RoutingEntity(id++,point);
            entity.setTotalPointsNum(points.size());
            if(point.equals(pointInputVo.getStart())){
                entity.setStart(true);
                entity.setOrder(0);
                orderRange.remove(0);
            }
            if(point.equals(pointInputVo.getEnd())){
                entity.setEnd(true);
                entity.setOrder(points.size()-1);
                orderRange.remove(orderRange.size()-1);
            }
            routing.add(entity);
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
    
}
