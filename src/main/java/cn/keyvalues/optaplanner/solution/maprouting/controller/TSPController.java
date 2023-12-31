package cn.keyvalues.optaplanner.solution.maprouting.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.common.constant.CommonConstant;
import cn.keyvalues.optaplanner.solution.maprouting.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.maprouting.service.TSPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tsproblem")
@Tag(name = "组合TSP问题的线路规划")
@Slf4j
public class TSPController {
    
    @Autowired
    TSPService tspService;

    @PostMapping("/solveAsync")
    @Operation(summary = "选客户点，选基地出发点，选策略。 异步求解更优的规划方案",description = "返回problemID")
    public Result<?> solveAsync(@RequestBody ProblemInputVo problemInputVo){
        Result<?> result;
        try{
            return tspService.solveAsync(problemInputVo);
        }catch(Exception e){
            log.error(e.getMessage());
            result=Result.failed(CommonConstant.FAILED,e.getMessage());
        }
        return result;
    }

    @GetMapping("/solveAsync/pollUpdate")
    @Operation(summary = "获取/消费solveAsync结果队列")
    public Result<?> solveAysncPollUpdate(@RequestParam String problemID,@Parameter(description = "轮询间隔时间(单位毫秒)") @RequestParam long intervalTime){
        Result<?> result;
        try{
            return Result.OK(tspService.pollUpdate(UUID.fromString(problemID),intervalTime));
        }catch(Exception e){
            log.error(e.getMessage());
            result=Result.failed(CommonConstant.FAILED,e.getMessage());
        }
        return result;
    }

    @GetMapping("/terminalProblem")
    @Operation(summary = "立即终止问题并获取结果")
    public Result<?> terminalProblem(@RequestParam String problemID){
        Map<String,Object> data = tspService.terminalProblem(UUID.fromString(problemID),true);
        return Result.OK(data);
    }

    @GetMapping("/listProblem")
    @Operation(summary = "列出问题")
    public Result<?> listProblem(){
        List<Map<String,Object>> data=tspService.listProblem();
        return Result.OK(data);
    }

    @GetMapping("/deleteProblem")
    @Operation(summary = "删除某个问题记录")
    public Result<?> deleteProblem(@RequestParam String problemID){
        return Result.OK(tspService.deleteProblem(UUID.fromString(problemID)));
    }

}
