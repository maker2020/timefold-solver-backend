package cn.keyvalues.optaplanner.solution.cflp.service.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationConstraintConfig;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import cn.keyvalues.optaplanner.solution.cflp.domain.entity.CFLPSolutionEntity;
import cn.keyvalues.optaplanner.solution.cflp.proxy.FacilityConstraintProxyWrapper;
import cn.keyvalues.optaplanner.solution.cflp.service.CFLPService;
import cn.keyvalues.optaplanner.solution.cflp.service.CFLPSolutionService;
import cn.keyvalues.optaplanner.solution.cflp.solver.FacilityLocationConstraint;
import cn.keyvalues.optaplanner.utils.BeanUtils;
import cn.keyvalues.optaplanner.utils.planner.SolutionHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class CFLPServiceImpl implements CFLPService{

    private final SolverConfig solverConfig;
    private SolutionHelper solutionHelper;
    private CFLPSolutionService solutionService;

    public CFLPServiceImpl(@Qualifier("cflpConfig") SolverConfig solverConfig,
            SolutionHelper solutionHelper,CFLPSolutionService solutionService,
            HttpServletRequest request) {
        this.solverConfig = solverConfig;
        this.solutionHelper=solutionHelper;
        this.solutionService=solutionService;
    }

    // transactional
    @Override
    public Result<?> solveAsync(ProblemInputVo problemInputVo) {
        FacilityLocationSolution initializedSolution = generateSolution(problemInputVo);
        try {
            solutionHelper.defineConstraintConfig(initializedSolution,problemInputVo.getConstraintConfig(),HardMediumSoftLongScore.class);
        } catch (NoSuchFieldException e) {
            return Result.failed("约束配置异常");
        } catch (Exception e){
            return Result.failed(e.getMessage());
        }
        
        // 动态配置约束
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setInitializingScoreTrend("ANY");
        scoreDirectorFactoryConfig.setConstraintProviderClass(FacilityConstraintProxyWrapper.class);
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        solverConfig.setTerminationConfig(new TerminationConfig().withSecondsSpentLimit(problemInputVo.getTimeLimit()));
        // 随机问题ID，用于跟踪问题
        UUID problemID=UUID.randomUUID();
        // 保存问题
        CFLPSolutionEntity entity=new CFLPSolutionEntity();
        entity.setProblemId(problemID.toString());
        entity.setAssigns(initializedSolution.getAssigns());
        BeanUtils.copyPropertiesSpring(problemInputVo, entity);
        solutionService.save(entity);

        solutionHelper.solveAsync(initializedSolution, solverConfig, problemID, update->{
            // 求解记录放入队列前的处理
            // do nothing
        }, update->{
            // 持久化存储的更新...
            CFLPSolutionEntity solution = solutionService.getOne(new QueryWrapper<CFLPSolutionEntity>().eq("problem_id", problemID.toString()));
            solution.setAssigns(update.getAssigns());
            solution.setCustomers(update.getCustomers());
            solution.setServerStations(update.getServerStations());
            solution.setStatus(SolverStatus.SOLVING_ACTIVE.toString());
            solution.setScore(update.getScore().toString());
            solutionService.saveOrUpdate(solution);
        }, lastSolution->{
            // 持久化存储的更新...
            CFLPSolutionEntity solution=solutionService.getOne(new QueryWrapper<CFLPSolutionEntity>().eq("problem_id", problemID.toString()));
            solution.setStatus(SolverStatus.NOT_SOLVING.toString());
            solution.setScore(lastSolution.getScore().toString());
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

    @Override
    public Map<String, Object> terminalProblem(UUID problemID, boolean save) {
        return solutionHelper.terminalProblem(problemID, null, null);
    }

    @Override
    public void deleteStationRealTime(UUID problemID,final long stationID) {
        ServerStation station=new ServerStation();
        station.setId(stationID);
        SolverManager<FacilityLocationSolution,UUID> solverManager = solutionHelper.getSolverManager(problemID, FacilityLocationSolution.class);
        solverManager.addProblemChange(problemID, (workingSolution,problemChangeDirector)->{
            problemChangeDirector.lookUpWorkingObject(station).ifPresentOrElse(workingStation->{
                // 1. First remove the problem fact from all planning entities that use it
                for (Assign assign : workingSolution.getAssigns()) {
                    if(assign.getStation()==workingStation){
                        problemChangeDirector.changeVariable(assign, "station", 
                                workingAssign->workingAssign.setStation(null));
                    }
                }
                // 2. A SolutionCloner does not clone problem fact lists (such as computerList)
                // Shallow clone the computerList so only workingSolution is affected, not bestSolution or guiSolution
                // 此处不能深克隆，不然bestSolution等阶段变量都会改变。
                ArrayList<ServerStation> stationList=new ArrayList<>(workingSolution.getServerStations());
                workingSolution.setServerStations(stationList);
                // 3. Remove the problem fact itself
                problemChangeDirector.removeProblemFact(workingStation, stationList::remove);
            }, ()->{
                throw new IllegalStateException("station @id:"+stationID+" is not present.");
            });
        });
    }

    @Override
    public void deleteCustomerRealTime(UUID problemID, long customerID) {
        Customer customer=new Customer();
        customer.setId(customerID);
        SolverManager<FacilityLocationSolution,UUID> solverManager = solutionHelper.getSolverManager(problemID, FacilityLocationSolution.class);
        solverManager.addProblemChange(problemID, (workingSolution,problemChangeDirector)->{
            problemChangeDirector.lookUpWorkingObject(customer).ifPresentOrElse(workingCustomer->{
                for(Assign assign:workingSolution.getAssigns()){
                    if(assign.getCustomer()==workingCustomer){
                        problemChangeDirector.changeVariable(assign, "customer", 
                                workingAssign->workingAssign.setCustomer(null));
                    }
                }
                ArrayList<Customer> customerList=new ArrayList<>(workingSolution.getCustomers());
                workingSolution.setCustomers(customerList);
                problemChangeDirector.removeProblemFact(workingCustomer, customerList::remove);
            }, ()->{
                throw new IllegalStateException("customer @id:"+customerID+" is not present.");
            });
        });
    }

    @Override
    public void addStationRealTime(UUID problemID, ServerStation newStation) throws RuntimeException {
        // SolverManager<FacilityLocationSolution,UUID> solverManager = solutionHelper.getSolverManager(problemID, FacilityLocationSolution.class);
        // solverManager.addProblemChange(problemID, (workingSolution,problemChangeDirector)->{
        //     long maxID = workingSolution.getServerStations().stream().mapToLong(ServerStation::getId).max().getAsLong();
        //     // 待研究它的机制是否有并发问题
        //     newStation.setId(maxID+1);
        //     // 以m*n，扩充Assign
        //     List<Assign> assigns = workingSolution.getAssigns();
        //     // 不确定是不是能扩充entity，待issue回复再写这个。
        //     long addLength=workingSolution.getCustomers().size();
            
        // });
    }

    @Override
    public void addCustomerRealTime(UUID problemID, Customer newCustomer) throws RuntimeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addCustomerRealTime'");
    }

    @Override
    public List<Map<String, Object>> listProblem() {
        List<Map<String,Object>> result=new ArrayList<>();
        List<CFLPSolutionEntity> list = solutionService.list();
        for (CFLPSolutionEntity entity : list) {
            Map<String, Object> solution = BeanUtils.objectToMap(entity);
            // 对customers和serverStation单独处理。
            // ...
            solution.put("customers", FacilityLocationSolution.releaseCustomers(entity.getCustomers()));
            solution.put("serverStations", FacilityLocationSolution.releaseStations(entity.getServerStations()));
            solution.put("assigns", FacilityLocationSolution.releaseAssign(entity.getAssigns()));
            result.add(solution);
        }
        return result;
    }

    @Override
    public boolean deleteProblem(UUID problemID) {
        return solutionService.remove(new QueryWrapper<CFLPSolutionEntity>().eq("problem_id", problemID.toString()));
    }

    @Override
    public List<String> listConstraints() {
        Class<FacilityLocationConstraintConfig> clazz = FacilityLocationConstraintConfig.class;
        return Arrays.stream(clazz.getFields()).map(field->{
            try {
                return field.get(null).toString();
            } catch (Exception e){
                return "error";
            }
        }).toList();
    }

    @Override
    public List<Map<String,Object>> listDefinedConstraints() {
        Class<FacilityLocationConstraint> clazz = FacilityLocationConstraint.class;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Map<String,Object>> list=new ArrayList<>();
        for(Method method:declaredMethods){
            if(method.getName().equals("defineConstraints") ||
                    method.getName().contains("$")){
                continue;
            }
            Map<String,Object> obj=new HashMap<>();
            obj.put("constraintID", method.getName());
            obj.put("descriptionCN", method.getAnnotation(Schema.class).description());
            list.add(obj);
        }
        return list;
    }
    
}
