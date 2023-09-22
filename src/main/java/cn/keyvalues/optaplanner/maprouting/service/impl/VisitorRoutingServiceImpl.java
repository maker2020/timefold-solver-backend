package cn.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.constant.RedisConstant;
import cn.keyvalues.optaplanner.geo.Point;
import cn.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import cn.keyvalues.optaplanner.maprouting.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.maprouting.domain.Location;
import cn.keyvalues.optaplanner.maprouting.domain.Visitor;
import cn.keyvalues.optaplanner.maprouting.domain.VisitorBase;
import cn.keyvalues.optaplanner.maprouting.domain.VisitorRoutingSolution;
import cn.keyvalues.optaplanner.maprouting.service.VisitorRoutingService;
import cn.keyvalues.optaplanner.utils.RedisUtil;

@Service
public class VisitorRoutingServiceImpl implements VisitorRoutingService{

    public static RedisUtil redisUtil;

    private Map<UUID,SolverJob<VisitorRoutingSolution,UUID>> solverJobMap;
    private final SolverConfig solverConfig;
    private final BaiduDirection baiduDirection;

    private Map<UUID,ConcurrentLinkedDeque<Map<String,Object>>> solverSolutionQueue=new ConcurrentHashMap<>(); 

    public VisitorRoutingServiceImpl(SolverManager<VisitorRoutingSolution, UUID> solverManager,SolverConfig solverConfig,BaiduDirection baiduDirection,RedisUtil redisUtil) {
        solverJobMap=new ConcurrentHashMap<>();
        this.solverConfig = solverConfig;
        this.baiduDirection = baiduDirection;
        VisitorRoutingServiceImpl.redisUtil=redisUtil;
    }

    @Override
    public Result<?> solveAsync(ProblemInputVo problemInputVo) {
        // 构建问题
        VisitorRoutingSolution initializedSolution = generateSolution(problemInputVo);
        // 构建P2P制定策略的优化值，Redis保存
        createOptimalValueMap(initializedSolution);
        // 全局保存
        // VisitorRoutingController.p2pOptimalValueMap.putAll(optimalValMap);     
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

    @Override
    public void removeProblem(UUID problemID) {
        // 判断问题存在就终止
        if(solverJobMap.containsKey(problemID)){
            SolverJob<VisitorRoutingSolution,UUID> solverJob = solverJobMap.get(problemID);
            solverJob.terminateEarly();
        }
        // 从管理对象中移除
        solverJobMap.remove(problemID);
        solverSolutionQueue.remove(problemID);
    }

    @Override
    public Map<String, Object> terminalProblem(UUID problemID) {
        Map<String,Object> result=null;
        // 判断问题不存在就返回null
        if(!solverJobMap.containsKey(problemID)){
            return null;
        }
        SolverJob<VisitorRoutingSolution,UUID> solverJob = solverJobMap.get(problemID);
        // 阻塞到退出
        solverJob.terminateEarly();
        ConcurrentLinkedDeque<Map<String,Object>> solutionDeque = solverSolutionQueue.get(problemID);
        // 没有结果就也返回null
        if(solutionDeque==null || solutionDeque.size()==0) {
            return null;
        }
        Map<String,Object> lastSolution = solutionDeque.getLast();
        VisitorRoutingSolution solution=(VisitorRoutingSolution)lastSolution.get("updatedSolution");
        // 拿到后不清除，用户通过专门清除接口来清除
        // solverJobMap.remove(problemID);
        // solverSolutionQueue.remove(problemID);
        result=new HashMap<>();
        result.put("solution", solution.getNoEachReferenceSolution(null));
        return result;
    }

    @Override
    public List<Map<String, Object>> listProblem() {
        List<Map<String,Object>> problemList=new ArrayList<>();
        Set<UUID> problemIDSet = solverJobMap.keySet();
        for(UUID problemID:problemIDSet){
            SolverJob<VisitorRoutingSolution,UUID> solverJob=solverJobMap.get(problemID);
            Map<String,Object> solutionObj=new HashMap<>();
            solutionObj.put("problemID", problemID);

            SolverStatus solverStatus=solverJob.getSolverStatus();
            solutionObj.put("status", solverStatus);

            ConcurrentLinkedDeque<Map<String,Object>> solutionQueue = solverSolutionQueue.get(problemID);
            Map<String,Object> latestSolution = solutionQueue.peekLast();
            VisitorRoutingSolution solution=latestSolution==null?null:(VisitorRoutingSolution)latestSolution.get("updatedSolution");
            solutionObj.put("suolution", solution.getNoEachReferenceSolution(null));

            problemList.add(solutionObj);
        }
        return problemList;
    }

    private VisitorRoutingSolution generateSolution(ProblemInputVo problemInputVo){
        VisitorRoutingSolution solution=new VisitorRoutingSolution();
        
        // 客户和点位初始化：id由后端生成
        long id=0L;
        List<Customer> customers = problemInputVo.getCustomers();
        for(Customer customer:customers){
            customer.setId(id);
            customer.getLocation().setId(id);
            id++;
        }
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
     * 根据问题和策略生成对应策略最优值的p2p映射图，并存入redis缓存
     * @param initializedSolution 初始化了的solution
     * @param tactics API策略选项
     * @return
     */
    private void createOptimalValueMap(VisitorRoutingSolution initializedSolution){
        class Combine{
            /**
             * 不会改变元素的组合
             */
            static <T> void dfs(List<T> elements, int choose, int start, List<T> current, List<List<T>> combinationCollection) {
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
        // 客户之间组合
        List<Customer> customerList = initializedSolution.getCustomerList();
        List<List<Customer>> combinationList = new ArrayList<>();
        Combine.dfs(customerList, 2, 0, new ArrayList<>(), combinationList);

        // 各起点->各客户点的组合 （如果考虑回去的终点要做新组合）
        // 将起点拟作一个Customer统一放一个List<List>
        List<List<Customer>> combinationList2 = new ArrayList<>();
        List<VisitorBase> visitorBases=initializedSolution.getVisitorBases();
        List<Point> points=visitorBases.stream().map(r->r.getLocation().getPoint()).toList();
        for(Point origin:points){
            List<Customer> item=new ArrayList<>();
            Customer cOirgin=new Customer(-1, new Location(-1, origin));
            item.add(cOirgin);
            for(Customer to:customerList){
                item.add(to);
            }
            combinationList2.add(item);
        }
        combinationList.addAll(combinationList2);

        // 构建策略最优值p2p图
        // 约束中算值是每个客户的上一个，因此当遇到a->b，则表示客户b的Tactisc/策略
        // base拟为customer的默认距离最短（不参与计算无影响）
        for (List<Customer> cc : combinationList) {
            Customer a=cc.get(0);
            Customer b=cc.get(1);
            Point p0=a.getLocation().getPoint();
            Point p1=b.getLocation().getPoint();
            StringBuilder sb=new StringBuilder();
            // a->b
            String key0=sb.append(p0.toString()).append("->").append(p1.toString()).append(":").append(b.getTactics()).toString();
            long optimalValue0;
            // 先判断缓存
            if(redisUtil.hHasKey(RedisConstant.p2pOptimalValueMap, key0)){
                optimalValue0=(long)redisUtil.hget(RedisConstant.p2pOptimalValueMap,key0);
            }else{
                optimalValue0 = baiduDirection.calculateOptimalValue(p0, p1, b.getTactics());
                redisUtil.hset(RedisConstant.p2pOptimalValueMap, key0, optimalValue0, 1800);
            }
            sb.setLength(0);
            // b->a
            String key1=sb.append(p1.toString()).append("->").append(p0.toString()).append(":").append(a.getTactics()).toString();
            long optimalValue1;
            if(redisUtil.hHasKey(RedisConstant.p2pOptimalValueMap, key1)){
                optimalValue1=(long)redisUtil.hget(RedisConstant.p2pOptimalValueMap,key1);
            }else{
                optimalValue1 = baiduDirection.calculateOptimalValue(p1, p0, a.getTactics());
                redisUtil.hset(RedisConstant.p2pOptimalValueMap, key1, optimalValue1, 1800);
            }
        }
    }

}
