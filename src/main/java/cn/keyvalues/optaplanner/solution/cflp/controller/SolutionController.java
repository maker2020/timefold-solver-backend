package cn.keyvalues.optaplanner.solution.cflp.controller;

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
import cn.keyvalues.optaplanner.constant.CommonConstant;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.cflp.service.CFLPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "设施位置问题的优化求解")
@RequestMapping("/cflproblem")
@Slf4j
public class SolutionController {
    
    @Autowired
    CFLPService cflpService;

    @PostMapping("/solveAsync")
    @Operation(summary = "根据客户、服务站，求解优化方案",description = "返回problemID")
    public Result<?> solveAsync(@RequestBody ProblemInputVo problemInputVo){
        Result<?> result=null;
        try{
            result=cflpService.solveAsync(problemInputVo);
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
            return Result.OK(cflpService.pollUpdate(UUID.fromString(problemID),intervalTime));
        }catch(Exception e){
            log.error(e.getMessage());
            result=Result.failed(CommonConstant.FAILED,e.getMessage());
        }
        return result;
    }

    @GetMapping("/terminalProblem")
    @Operation(summary = "立即终止问题并获取结果")
    public Result<?> terminalProblem(@RequestParam String problemID){
        Map<String,Object> data = cflpService.terminalProblem(UUID.fromString(problemID),false);
        return Result.OK(data);
    }

}
