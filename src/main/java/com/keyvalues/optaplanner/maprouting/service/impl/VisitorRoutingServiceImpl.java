package com.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.stereotype.Service;

import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.common.enums.TacticsEnum;
import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import com.keyvalues.optaplanner.maprouting.controller.VisitorRoutingController;
import com.keyvalues.optaplanner.maprouting.controller.vo.ProblemInputVo;
import com.keyvalues.optaplanner.maprouting.domain.Customer;
import com.keyvalues.optaplanner.maprouting.domain.Location;
import com.keyvalues.optaplanner.maprouting.domain.Visitor;
import com.keyvalues.optaplanner.maprouting.domain.VisitorBase;
import com.keyvalues.optaplanner.maprouting.domain.VisitorRoutingSolution;
import com.keyvalues.optaplanner.maprouting.service.VisitorRoutingService;

@Service
public class VisitorRoutingServiceImpl implements VisitorRoutingService{

    private Map<UUID,SolverJob<VisitorRoutingSolution,UUID>> solverJobMap;
    private final SolverConfig solverConfig;
    private final BaiduDirection baiduDirection;

    private Map<UUID,ConcurrentLinkedDeque<Map<String,Object>>> solverSolutionQueue=new ConcurrentHashMap<>(); 

    public VisitorRoutingServiceImpl(SolverManager<VisitorRoutingSolution, UUID> solverManager,SolverConfig solverConfig,BaiduDirection baiduDirection) {
        solverJobMap=new ConcurrentHashMap<>();
        this.solverConfig = solverConfig;
        this.baiduDirection = baiduDirection;
    }

    @Override
    public Result<?> solveAsync(ProblemInputVo problemInputVo) {
        // 构建问题
        VisitorRoutingSolution initializedSolution = generateSolution(problemInputVo);
        // 构建P2P制定策略的优化值
        Map<String,Long> optimalValMap = createOptimalValueMapMap(initializedSolution,TacticsEnum.TWO);
        // 全局保存
        VisitorRoutingController.p2pOptimalValueMap.putAll(optimalValMap);
        // 写入可选配置、初始化相关管理对象
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(problemInputVo.getTimeLimit()));
        SolverFactory<VisitorRoutingSolution> factory = SolverFactory.create(solverConfig);
        SolverManager<VisitorRoutingSolution,UUID> solverManager = SolverManager.create(factory,new SolverManagerConfig());
        ConcurrentLinkedDeque<Map<String,Object>> syncQueue=new ConcurrentLinkedDeque<Map<String,Object>>();
        UUID problemID=UUID.randomUUID();
        solverSolutionQueue.put(problemID, syncQueue);
        SolverJob<VisitorRoutingSolution,UUID> solverJob=solverManager.solveAndListen(problemID,r->{return initializedSolution;},(update)->{
            Map<String,Object> newData=new HashMap<>();
            newData.put("status", solverManager.getSolverStatus(problemID));
            newData.put("updatedSolution", update);
            syncQueue.add(newData);
        });
        // 可跟踪问题状态、最终解
        solverJobMap.put(problemID, solverJob);

        Map<String,Object> data=new HashMap<>();
        data.put("problemID", problemID.toString());
        return Result.OK("请求成功！正在后台处理...", data);
    }

    @Override
    public Map<String, Object> pollUpdate(UUID problemID) throws Exception {
        Map<String,Object> data=new HashMap<>();
        ConcurrentLinkedDeque<Map<String,Object>> solveQueue=solverSolutionQueue.get(problemID);
        // 问题不存在
        if(solveQueue==null) return null;
        Map<String,Object> problemData=solveQueue.poll();
        SolverJob<VisitorRoutingSolution,UUID> solverJob=solverJobMap.get(problemID);
        if(problemData==null && solverJob.getSolverStatus().equals(SolverStatus.NOT_SOLVING)){
            data.put("status", SolverStatus.NOT_SOLVING);

            // 清空该问题对应的资源 (该更新接口已经被浏览器最后一次拿到结果，说明客户端已看到，可以释放)
            solverSolutionQueue.remove(problemID);
            solverJobMap.remove(problemID);
            return data;
        }
        // 说明暂时没更好的结果,但还在计算
        if(problemData==null && SolverStatus.SOLVING_ACTIVE.equals(solverJob.getSolverStatus())){
            data.put("status", SolverStatus.SOLVING_ACTIVE);
            return data;
        }
        VisitorRoutingSolution solution=(VisitorRoutingSolution)problemData.get("updatedSolution");
        SolverStatus status=(SolverStatus)problemData.get("status");

        data.put("solution", solution.getNoEachReferenceSolution(null));
        data.put("status", status);
        return data;
    }

    private VisitorRoutingSolution generateSolution(ProblemInputVo problemInputVo){
        VisitorRoutingSolution solution=new VisitorRoutingSolution();
        
        // 点位和客户初始化：id由后端生成
        long id=0L;
        List<Location> locations = problemInputVo.getLocationList();
        List<Customer> customers = new ArrayList<>();
        for(Location location:locations){
            location.setId(id);
            // 客户
            Customer customer=new Customer(id,location);
            customers.add(customer);
            id++;
        }
        solution.setLocationList(locations);
        solution.setCustomerList(customers);

        // 访问者/车辆 、出发点/基地（及其点位） 的设置
        long id_=0L;
        List<Visitor> visitors=problemInputVo.getVisitors();
        List<VisitorBase> bases=new ArrayList<>();
        for(Visitor visitor:visitors){
            VisitorBase base = visitor.getBase();
            Location baseLocation=base.getLocation();
            base.setId(id_);
            baseLocation.setId(id_);
            visitor.setId(id_);
            bases.add(base);
            id_++;
        }
        solution.setVisitorList(visitors);
        solution.setVisitorBases(bases);
        return solution;
    }

    /**
     * 根据问题和策略生成对应策略最优值的p2p映射图
     * @param initializedSolution 初始化了的solution
     * @param tactics API策略选项
     * @return ()->():xx => Long
     */
    private Map<String,Long> createOptimalValueMapMap(VisitorRoutingSolution initializedSolution,TacticsEnum tactics){
        class Combine{
            /**
             * 不会改变元素的组合
             */
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
        // 客户点之间组合
        List<Location> locationList = initializedSolution.getLocationList();
        List<Point> locationToPoints = locationList.stream().map(r->r.getPoint()).toList();
        List<List<Point>> combinationList = new ArrayList<>();
        Combine.dfs(locationToPoints, 2, 0, new ArrayList<>(), combinationList);
        // 各起点->各客户点的组合 （如果考虑回去的终点要做新组合）
        List<List<Point>> combinationList2 = new ArrayList<>();
        List<VisitorBase> visitorBases=initializedSolution.getVisitorBases();
        List<Point> points=visitorBases.stream().map(r->r.getLocation().getPoint()).toList();
        for(Point origin:points){
            List<Point> item=new ArrayList<>();
            item.add(origin);
            for(Point to:locationToPoints){
                item.add(to);
            }
            combinationList2.add(item);
        }
        combinationList.addAll(combinationList2);

        // 构建策略最优值p2p图
        Map<String,Long> p2pOptimalValueMap=new HashMap<>();
        for (List<Point> point : combinationList) {
            Point a=point.get(0);
            Point b=point.get(1);
            StringBuilder sb=new StringBuilder();
            // a->b
            long optimalValue0 = baiduDirection.calculateOptimalValue(a, b, tactics);
            String key0=sb.append(a.toString()).append("->").append(b.toString()).append(":").append(tactics).toString();
            p2pOptimalValueMap.put(key0, optimalValue0);
            sb.setLength(0);
            // b->a
            long optimalValue1 = baiduDirection.calculateOptimalValue(b, a, tactics);
            String key1=sb.append(b.toString()).append("->").append(a.toString()).append(":").append(tactics).toString();
            p2pOptimalValueMap.put(key1, optimalValue1);
        }
        return p2pOptimalValueMap;
    }

}
