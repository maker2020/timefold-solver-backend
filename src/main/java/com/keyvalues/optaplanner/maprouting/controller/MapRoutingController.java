package com.keyvalues.optaplanner.maprouting.controller;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.maprouting.controller.vo.PointInputVo;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.service.SolverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "线路规划")
@RestController
@Slf4j
@RequestMapping("/maprouting")
public class MapRoutingController {

    /**
     * 暂时这么用，后面统一管理
     * 点距离关系 缓存redis可
     */
    public static final Map<String,Integer> p2pDistanceMap=new ConcurrentHashMap<>();

    @Autowired
    private SolverService solverService;

    @GetMapping("/test")
    public Result<?> test(){
        return Result.OK("test");
    }

    @PostMapping("/solve")
    @Operation(summary = "根据选点求解优化线路")
    public Result<?> solve(@RequestBody PointInputVo pointInputVo){
        MapRoutingSolution solution = solverService.mapRoutingSolve(pointInputVo);
        return Result.OK(solution);
    }

    @PostMapping("/solveAsync")
    @Operation(summary = "根据选点求解优化线路(异步求解，轮询获取最新计算结果)")
    public Result<?> solveAsync(@RequestBody PointInputVo pointInputVo){
        Result<?> result = solverService.mapRoutingSolveAsync(pointInputVo);
        return result;
    }

    @GetMapping("/poll-update")
    @Operation(summary = "同步最新计算分数")
    public Result<?> pollUpdate(@RequestParam String problemID){
        try {
            Map<String,Object> lastestData = solverService.pollUpdate(UUID.fromString(problemID));
            return Result.OK(lastestData);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.failed("计算异常，无法获取最新数据");
        }

    }

}
