package cn.keyvalues.optaplanner.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.keyvalues.optaplanner.common.CircularRefRelease;
import lombok.extern.slf4j.Slf4j;

/**
 * 帮助维护求解记录、求解状态等、及内存管理
 */
@Component
// @DependsOn("utils")
@Slf4j
public class SolutionHelper {

    private final Map<UUID,ConcurrentLinkedDeque<Map<String,Object>>> solverSolutionQueueMap; 
    private final Map<UUID,SolverManager<?,UUID>> solverManagerMap;

    public static final String STATUS_KEY="status";
    public static final String UPDATED_KEY="updatedSolution";
    public static final String SOLUTION_KEY="solution";

    public static final long CLEAR_DELAY=600; // s

    public SolutionHelper(){
        this.solverSolutionQueueMap=new ConcurrentHashMap<>(); 
        this.solverManagerMap=new ConcurrentHashMap<>();
    }

    /**
     * 求解已构造的问题，并返回UUID作为问题ID
     * @param <T>
     * @param initializedSolution
     * @param config
     * @param bestSolutionConsumerAfter 结果更新之后的回调，可以做数据保存
     * @param resolvedConsumer 求解完成的回调
     */
    public <T> SolverJob<T,UUID> solveAsync(T initializedSolution,SolverConfig config,UUID problemID,
            Consumer<T> bestConsumerBefore,Consumer<? super T> bestSolutionConsumerAfter,Consumer<? super T> resolvedConsumer){
        SolverFactory<T> factory = SolverFactory.create(config);
        SolverManager<T,UUID> solverManager = SolverManager.create(factory);
        ConcurrentLinkedDeque<Map<String,Object>> syncQueue=new ConcurrentLinkedDeque<Map<String,Object>>();
        solverSolutionQueueMap.put(problemID, syncQueue);
        solverManagerMap.put(problemID, solverManager);
        Consumer<T> consumer=update->{
            Map<String,Object> newData=new HashMap<>();
            newData.put(STATUS_KEY, solverManager.getSolverStatus(problemID));
            newData.put(UPDATED_KEY, update);
            syncQueue.add(newData);
        };
        Consumer<T> bestConsumer=consumer.andThen(bestSolutionConsumerAfter);
        Consumer<T> bestConsumerBegin=bestConsumerBefore.andThen(bestConsumer);

        Consumer<T> finalConsumer=(finalSolution)->{
            Map<String,Object> newData=new HashMap<>();
            newData.put(STATUS_KEY, SolverStatus.NOT_SOLVING);
            newData.put(UPDATED_KEY, finalSolution);
            syncQueue.add(newData);
            log.info("[problem:"+problemID+"] has been done");
            // 问题求解完后10分钟清理
            ScheduledExecutorService executorService=Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(()->{
                // 清空管理对象
                solverSolutionQueueMap.remove(problemID);
                solverManagerMap.remove(problemID);
            }, CLEAR_DELAY, TimeUnit.SECONDS);
            try {
                executorService.awaitTermination(CLEAR_DELAY*2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {}
        };
        Consumer<T> endConsumer=finalConsumer.andThen(resolvedConsumer);
        SolverJob<T,UUID> solverJob=solverManager.solveAndListen(problemID, 
                r->initializedSolution, bestConsumerBegin,endConsumer,(pid,except)->{
                    solverSolutionQueueMap.remove(problemID);
                    solverManagerMap.remove(problemID);
                    log.error(pid.toString(),except);
                });
        return solverJob;
    }

    /**
     * 拉去某个问题求解的解记录
     * @param problemID
     * @param intervalTime
     */
    public Map<String,Object> pollUpdate(UUID problemID, long intervalTime){
        // 拉取数据
        Map<String,Object> data=new HashMap<>();
        ConcurrentLinkedDeque<Map<String,Object>> solveQueue=solverSolutionQueueMap.get(problemID);
        SolverManager<?,UUID> solverManager=(SolverManager<?,UUID>)solverManagerMap.get(problemID);
        Map<String,Object> problemData=solveQueue==null?null:solveQueue.poll();
        SolverStatus instaneousStatus;
        if(solverManager==null || solveQueue==null){
            instaneousStatus=SolverStatus.NOT_SOLVING;
        }else{
            // 如果求解完成必然有一个携带NOT_SOLVING的记录（NOT_SOLVING后都是无效轮询）
            if(problemData!=null){
                instaneousStatus=(SolverStatus)problemData.get(STATUS_KEY);
                if(problemData.get(UPDATED_KEY) instanceof CircularRefRelease ref){
                    data.put(SOLUTION_KEY, JSON.toJSONString(ref.releaseCircular(),SerializerFeature.DisableCircularReferenceDetect));
                }else{
                    data.put(SOLUTION_KEY, JSON.toJSONString(problemData.get(UPDATED_KEY),SerializerFeature.DisableCircularReferenceDetect));
                }
            }else{
                instaneousStatus=SolverStatus.SOLVING_ACTIVE;
            }
        }
        data.put(STATUS_KEY, instaneousStatus);
        return data;
    }

    @SuppressWarnings("unchecked")
    public <Solution_> SolverManager<Solution_, UUID> getSolverManager(UUID problemID,Class<Solution_> solution) throws RuntimeException{
        Object obj=solverManagerMap.get(problemID);
        return obj==null?null:(SolverManager<Solution_, UUID>)obj;
    }

    public ConcurrentLinkedDeque<Map<String, Object>> getSolutionQueue(UUID problemID) {
        return solverSolutionQueueMap.get(problemID);
    }

    public SolverStatus getStatus(UUID problemID){
        Object obj = solverManagerMap.get(problemID);
        return obj==null?SolverStatus.NOT_SOLVING:solverManagerMap.get(problemID).getSolverStatus(problemID);
    }

}
