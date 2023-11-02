package cn.keyvalues.optaplanner.solution.maprouting.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ai.timefold.solver.core.api.solver.SolverStatus;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.constant.RedisConstant;
import cn.keyvalues.optaplanner.geo.Point;
import cn.keyvalues.optaplanner.solution.maprouting.api.BaiduDirection;
import cn.keyvalues.optaplanner.solution.maprouting.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Location;
import cn.keyvalues.optaplanner.solution.maprouting.domain.Visitor;
import cn.keyvalues.optaplanner.solution.maprouting.domain.VisitorRoutingSolution;
import cn.keyvalues.optaplanner.solution.maprouting.domain.entity.TSPSolutionEntity;
import cn.keyvalues.optaplanner.solution.maprouting.service.TSPSolutionService;
import cn.keyvalues.optaplanner.solution.maprouting.service.TSPService;
import cn.keyvalues.optaplanner.utils.BeanUtils;
import cn.keyvalues.optaplanner.utils.SolutionHelper;

import static cn.keyvalues.optaplanner.utils.Utils.redisUtil;

@Service
public class TSPServiceImpl implements TSPService{

    SolutionHelper solutionHelper;
    TSPSolutionService solutionService;
    BaiduDirection baiduDirection;
    SolverConfig solverConfig;

    public TSPServiceImpl(SolutionHelper solutionHelper,BaiduDirection baiduDirection,
            @Qualifier("tspConfig") SolverConfig solverConfig,TSPSolutionService solutionService){
        this.solverConfig = solverConfig;
        this.baiduDirection = baiduDirection;
        this.solutionService=solutionService;
        this.solutionHelper=solutionHelper;
    }

    @Override
    public Result<?> solveAsync(ProblemInputVo problemInputVo) {
        VisitorRoutingSolution initializedSolution = generateSolution(problemInputVo);
        createOptimalValueMap(initializedSolution);
        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(problemInputVo.getTimeLimit()));
        UUID problemID=UUID.randomUUID();
        // 在求解开始前做持久化。
        TSPSolutionEntity solutionEntity=new TSPSolutionEntity();
        solutionEntity.setProblemId(problemID.toString());
        BeanUtils.copyPropertiesSpring(problemInputVo, solutionEntity);
        solutionService.save(solutionEntity);
        // 求解
        solutionHelper.solveAsync(initializedSolution, solverConfig, problemID, update->{
            // 为每个visitor的optimalMap字段赋值：即获取访问者的路径API数据
            update.getVisitorList().forEach(v->v.arrangeOptimalRelatedMap());
        }, update->{
            // 使数据库保持最新数据状态
            TSPSolutionEntity entity = solutionService.getOne(new QueryWrapper<TSPSolutionEntity>().eq("problem_id", problemID.toString()));
            entity.setVisitors(update.getVisitorList());
            entity.setStatus(SolverStatus.SOLVING_ACTIVE.toString());
            entity.setScore(update.getScore().toString());
            solutionService.saveOrUpdate(entity);
        }, finalSolution->{
            TSPSolutionEntity solution=solutionService.getOne(new QueryWrapper<TSPSolutionEntity>().eq("problem_id", problemID.toString()));
            solution.setStatus(SolverStatus.NOT_SOLVING.toString());
            solution.setScore(finalSolution.getScore().toString());
            solutionService.saveOrUpdate(solution);
        });
        Map<String,Object> data=new HashMap<>();
        data.put("problemID", problemID.toString());
        return Result.OK("请求成功！正在后台处理...", data);
    }

    @Override
    public Map<String, Object> pollUpdate(UUID problemID, long intervalTime) throws Exception {
        return solutionHelper.pollUpdate(problemID, intervalTime);
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
            Customer cOirgin=new Customer(-1, new Location(-1, origin));
            for(Customer to:customerList){
                List<Customer> item=new ArrayList<>();
                item.add(cOirgin);
                item.add(to);
                combinationList2.add(item);
            }
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

            // 存入缓存。

            // a->b
            String key0=sb.append(p0.toString()).append("->").append(p1.toString()).append(":").append(b.getLocation().getTactics()).toString();
            if(!redisUtil.hHasKey(RedisConstant.p2pOptimalValueMap, key0)){
                Map<String,Object> optimalMap=baiduDirection.calculateOptimalMap(p0, p1, b.getLocation().getTactics());
                redisUtil.hset(RedisConstant.p2pOptimalValueMap, key0, optimalMap, 1800);
            }

            sb.setLength(0);

            // b->a
            String key1=sb.append(p1.toString()).append("->").append(p0.toString()).append(":").append(a.getLocation().getTactics()).toString();
            if(!redisUtil.hHasKey(RedisConstant.p2pOptimalValueMap, key1)){
                Map<String,Object> optimalMap=baiduDirection.calculateOptimalMap(p1, p0, a.getLocation().getTactics());
                redisUtil.hset(RedisConstant.p2pOptimalValueMap, key1, optimalMap, 1800);
            }
        }
    }

    @Override
    public Map<String, Object> terminalProblem(UUID problemID,boolean save) {
        if(!save) {
            return solutionHelper.terminalProblem(problemID, null, null);
        } else {
            return solutionHelper.terminalProblem(problemID,VisitorRoutingSolution.class, solution->{
                    // 更新终止结果至数据库
                    TSPSolutionEntity entity=solutionService.getOne(new QueryWrapper<TSPSolutionEntity>().eq("problem_id", problemID.toString()));
                    entity.setStatus(SolverStatus.NOT_SOLVING.toString());
                    entity.setVisitors(solution.getVisitorList());
                    solutionService.saveOrUpdate(entity);
            });
        }
    }

    @Override
    public List<Map<String, Object>> listProblem() {
        List<Map<String,Object>> problemList=new ArrayList<>();
        List<TSPSolutionEntity> list = solutionService.list();
        for(TSPSolutionEntity entity:list){
            Map<String,Object> solution=BeanUtils.objectToMap(entity);
            // 对customers和visitors单独处理
            solution.put("customers", VisitorRoutingSolution.getNoEachReferenceCustomers(entity.getCustomers()));
            solution.put("visitors", VisitorRoutingSolution.getNoEachReferenceVisitors(entity.getVisitors()));
            problemList.add(solution);
        }
        return problemList;
    }

    @Override
    public boolean deleteProblem(UUID problemID) {
        terminalProblem(problemID,false);
        boolean deleted=solutionService.remove(new QueryWrapper<TSPSolutionEntity>().eq("problem_id", problemID.toString()));
        return deleted;
    }
    
}
