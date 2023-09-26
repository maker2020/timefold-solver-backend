package cn.keyvalues.optaplanner.maprouting.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.constant.RedisConstant;
import cn.keyvalues.optaplanner.geo.Point;
import cn.keyvalues.optaplanner.maprouting.api.BaiduDirection;
import cn.keyvalues.optaplanner.maprouting.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.maprouting.domain.Location;
import cn.keyvalues.optaplanner.maprouting.domain.Visitor;
import cn.keyvalues.optaplanner.maprouting.domain.VisitorRoutingSolution;
import cn.keyvalues.optaplanner.maprouting.domain.entity.SolutionEntity;
import cn.keyvalues.optaplanner.maprouting.service.SolutionService;
import cn.keyvalues.optaplanner.maprouting.service.VisitorRoutingService;
import cn.keyvalues.optaplanner.maprouting.utils.CircularRefUtil;
import cn.keyvalues.optaplanner.utils.RedisUtil;

@Service
public class VisitorRoutingServiceImpl implements VisitorRoutingService{

    public static RedisUtil redisUtil;

    private Map<UUID,SolverJob<VisitorRoutingSolution,UUID>> solverJobMap;
    private final SolverConfig solverConfig;
    private final BaiduDirection baiduDirection;
    private SolutionService solutionService;

    // 也可以考虑阻塞队列，浏览器递归请求
    //
    private Map<UUID,ConcurrentLinkedDeque<Map<String,Object>>> solverSolutionQueue=new ConcurrentHashMap<>(); 

    public VisitorRoutingServiceImpl(SolverManager<VisitorRoutingSolution, UUID> solverManager,SolverConfig solverConfig
            ,BaiduDirection baiduDirection,RedisUtil redisUtil,SolutionService solutionService) {
        solverJobMap=new ConcurrentHashMap<>();
        this.solverConfig = solverConfig;
        this.baiduDirection = baiduDirection;
        VisitorRoutingServiceImpl.redisUtil=redisUtil;
        this.solutionService=solutionService;
    }

    @Override
    // 事务
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
        // 在求解开始前做持久化。
        SolutionEntity solutionEntity=new SolutionEntity();
        solutionEntity.setCustomersJson(initializedSolution.getCustomerList());
        solutionEntity.setProblemId(problemID.toString());
        solutionEntity.setProblemName(problemInputVo.getProblemName());
        solutionEntity.setTimeLimit(problemInputVo.getTimeLimit());
        solutionEntity.setVisitorsJson(initializedSolution.getVisitorList());
        solutionService.save(solutionEntity);

        SolverJob<VisitorRoutingSolution,UUID> solverJob=solverManager.solveAndListen(problemID,r->{return initializedSolution;},(update)->{
            Map<String,Object> newData=new HashMap<>();
            SolverStatus status = solverManager.getSolverStatus(problemID);
            newData.put("status", status);
            newData.put("updatedSolution", update);
            syncQueue.add(newData);
            
            // 使数据库保持最新数据状态
            SolutionEntity entity = solutionService.getOne(new QueryWrapper<SolutionEntity>().eq("problem_id", problemID.toString()));
            entity.setVisitorsJson(update.getVisitorList());
            entity.setStatus(status.toString());
            solutionService.saveOrUpdate(entity);
        });
        // 开启线程阻塞获取最后结果和清空管理对象。用于更新数据库（solveListen回调没有最后的状态）
        new Thread(()->{
            try {
                solverJob.getFinalBestSolution(); // 不接收
            } catch (Exception e) {
                // ignore
            }
            SolutionEntity solution=solutionService.getOne(new QueryWrapper<SolutionEntity>().eq("problem_id", problemID.toString()));
            solution.setStatus(SolverStatus.NOT_SOLVING.toString());
            solutionService.saveOrUpdate(solution);
        }).start();
        // 可跟踪问题状态、最终解
        solverJobMap.put(problemID, solverJob);

        Map<String,Object> data=new HashMap<>();
        data.put("problemID", problemID.toString());
        return Result.OK("请求成功！正在后台处理...", data);
    }

    @Override
    public Map<String, Object> pollUpdate(UUID problemID,long intervalTime) throws Exception {
        // 根据轮询间隔时间，如果problemID已经发生请求，设定延迟3s没第二次请求就判定断开/超时，清空资源。
        // redis记录时间戳，定时器来完成清除资源
        // 允许请求正常间隔 仍延迟5s
        long allowDelay=intervalTime+5000;
        ScheduledExecutorService executorService=Executors.newSingleThreadScheduledExecutor();
        redisUtil.hset(RedisConstant.problemPollTimeMap, problemID.toString(), System.currentTimeMillis(), 30);
        executorService.scheduleWithFixedDelay(()->{
            Object val=redisUtil.hget(RedisConstant.problemPollTimeMap, problemID.toString());
            long lastRequestTimeMillis = val==null?0:(long)val;
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis-lastRequestTimeMillis>allowDelay){
                // 清空管理对象
                solverSolutionQueue.remove(problemID);
                solverJobMap.remove(problemID);
            }
        }, allowDelay, allowDelay, TimeUnit.MILLISECONDS);

        Map<String,Object> data=new HashMap<>();
        ConcurrentLinkedDeque<Map<String,Object>> solveQueue=solverSolutionQueue.get(problemID);
        SolverJob<VisitorRoutingSolution,UUID> solverJob=solverJobMap.get(problemID);
        Map<String,Object> problemData=solveQueue==null?null:solveQueue.poll();
        SolverStatus solverStatus = solverJob==null?SolverStatus.NOT_SOLVING:solverJob.getSolverStatus();
        if(problemData==null && SolverStatus.NOT_SOLVING.equals(solverStatus)){
            data.put("status", SolverStatus.NOT_SOLVING);
            return data;
        }
        // 说明暂时没更好的结果,但还在计算
        if(problemData==null && SolverStatus.SOLVING_ACTIVE.equals(solverStatus)){
            data.put("status", SolverStatus.SOLVING_ACTIVE);
            return data;
        }
        VisitorRoutingSolution solution=(VisitorRoutingSolution)problemData.get("updatedSolution");
        SolverStatus status=(SolverStatus)problemData.get("status");

        data.put("solution", CircularRefUtil.getNoEachReferenceSolution(solution,null));
        data.put("status", status);
        return data;
    }

    @Override
    public Map<String, Object> terminalProblem(UUID problemID,boolean save) {
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
        if(save){
            // 更新终止结果至数据库
            SolutionEntity entity=solutionService.getOne(new QueryWrapper<SolutionEntity>().eq("problem_id", problemID.toString()));
            entity.setStatus(SolverStatus.NOT_SOLVING.toString());
            entity.setVisitorsJson(solution.getVisitorList());
            solutionService.saveOrUpdate(entity);
        }

        // 并清除
        solverJobMap.remove(problemID);
        solverSolutionQueue.remove(problemID);

        result=new HashMap<>();
        result.put("solution", CircularRefUtil.getNoEachReferenceSolution(solution,null));
        return result;
    }

    @Override
    public List<Map<String, Object>> listProblem() {
        List<Map<String,Object>> problemList=new ArrayList<>();
        List<SolutionEntity> list = solutionService.list();
        for(SolutionEntity entity:list){
            Map<String,Object> solution=new HashMap<>();
            solution.put("problemID", entity.getProblemId());
            solution.put("problemName", entity.getProblemName());
            solution.put("score", entity.getScore());
            solution.put("status", entity.getStatus());
            solution.put("timeLimit", entity.getTimeLimit());
            solution.put("customers", CircularRefUtil.getNoEachReferenceCustomers(entity.getCustomersJson()));
            solution.put("visitors", CircularRefUtil.getNoEachReferenceVisitors(entity.getVisitorsJson()));
            problemList.add(solution);
        }
        return problemList;
    }

    @Override
    public boolean deleteProblem(UUID problemID) {
        boolean deleted=solutionService.remove(new QueryWrapper<SolutionEntity>().eq("problem_id", problemID.toString()));
        terminalProblem(problemID,false);
        return deleted;
    }

    private VisitorRoutingSolution generateSolution(ProblemInputVo problemInputVo){
        VisitorRoutingSolution solution=new VisitorRoutingSolution();
        
        // 客户和点位初始化：id由后端生成(只有规划实体需要设id)
        long id=0L;
        List<Customer> customers = problemInputVo.getCustomers();
        for(Customer customer:customers){
            customer.setId(id++);
        }
        solution.setCustomerList(customers);

        // 访问者/车辆 、出发点/基地（及其点位） 的设置
        long id_=0L;
        List<Visitor> visitors=problemInputVo.getVisitors();
        for(Visitor visitor:visitors){
            visitor.setId(id_++);
        }
        solution.setVisitorList(visitors);
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
        List<Point> points=initializedSolution.getVisitorList().stream().map(v->v.getBase().getLocation().getPoint()).toList();
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
            String key0=sb.append(p0.toString()).append("->").append(p1.toString()).append(":").append(b.getLocation().getTactics()).toString();
            long optimalValue0;
            // 先判断缓存
            if(redisUtil.hHasKey(RedisConstant.p2pOptimalValueMap, key0)){
                optimalValue0=(long)redisUtil.hget(RedisConstant.p2pOptimalValueMap,key0);
            }else{
                optimalValue0 = baiduDirection.calculateOptimalValue(p0, p1, b.getLocation().getTactics());
                redisUtil.hset(RedisConstant.p2pOptimalValueMap, key0, optimalValue0, 1800);
            }
            sb.setLength(0);
            // b->a
            String key1=sb.append(p1.toString()).append("->").append(p0.toString()).append(":").append(a.getLocation().getTactics()).toString();
            long optimalValue1;
            if(redisUtil.hHasKey(RedisConstant.p2pOptimalValueMap, key1)){
                optimalValue1=(long)redisUtil.hget(RedisConstant.p2pOptimalValueMap,key1);
            }else{
                optimalValue1 = baiduDirection.calculateOptimalValue(p1, p0, a.getLocation().getTactics());
                redisUtil.hset(RedisConstant.p2pOptimalValueMap, key1, optimalValue1, 1800);
            }
        }
    }

}
