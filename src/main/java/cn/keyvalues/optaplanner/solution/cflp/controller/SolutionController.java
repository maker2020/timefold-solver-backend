package cn.keyvalues.optaplanner.solution.cflp.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.keyvalues.optaplanner.common.Result;
import cn.keyvalues.optaplanner.common.constant.CommonConstant;
import cn.keyvalues.optaplanner.common.entity.ConstraintDefinition;
import cn.keyvalues.optaplanner.common.service.IConstraintDefinitionService;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo.OpMethodEnum;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo.OpTypeEnum;
import cn.keyvalues.optaplanner.solution.cflp.controller.vo.ProblemInputVo;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import cn.keyvalues.optaplanner.solution.cflp.service.CFLPService;
import cn.keyvalues.optaplanner.utils.Utils;
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

    @Autowired
    IConstraintDefinitionService definitionService;

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

    // 目前只支持cold start，要实现warm start需实现addProblemChange
    @GetMapping("/terminalProblem")
    @Operation(summary = "立即终止问题并获取结果")
    public Result<?> terminalProblem(@RequestParam String problemID){
        Map<String,Object> data = cflpService.terminalProblem(UUID.fromString(problemID),false);
        return Result.OK(data);
    }
    
    @GetMapping("/deleteStationRealTime")
    @Operation(summary = "warm restart 实时计算")
    public Result<?> deleteStationRealTime(String problemID,Long stationID){
        try{
            cflpService.deleteStationRealTime(UUID.fromString(problemID), stationID);
            return Result.OK();
        }catch(Exception e){
            return Result.failed(e.getMessage());
        }
    }

    @GetMapping("/deleteCustomerRealTime")
    @Operation(summary = "warm restart 实时计算")
    public Result<?> deleteCustomerRealTime(String problemID,Long stationID){
        try{
            cflpService.deleteCustomerRealTime(UUID.fromString(problemID), stationID);
            return Result.OK();
        }catch(Exception e){
            return Result.failed(e.getMessage());
        }
    }

    @GetMapping("/listConstraints")
    @Operation(summary = "约束列表")
    public Result<?> listConstraints(){
        return Result.OK(cflpService.listConstraints());
    }

    @GetMapping("/listDefinedConstrains")
    @Operation(summary = "程序已定义的约束列表")
    public Result<?> listDefinedConstrains(){
        return Result.OK(cflpService.listDefinedConstraints());
    }

    @GetMapping("/listStreamMethod")
    @Operation(summary = "操作方法列表")
    public Result<?> listStreamMethod(){
        return Result.OK(OpMethodEnum.values());
    }

    @GetMapping("/listStreamObject")
    @Operation(summary = "操作流类型列表")
    public Result<?> listStreamObject(){
        return Result.OK(OpTypeEnum.values());
    }

    @GetMapping("/listTargetClass")
    @Operation(summary = "目标类列表")
    public Result<?> listTargetClass(){
        // 从Solution读有problemFact的和planningEntity的，或者写死
        Map<String,Object> clazz=new HashMap<>();
        clazz.put("cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation", Arrays.stream(ServerStation.class.getDeclaredFields()).map(f->f.getName()).toList());
        clazz.put("cn.keyvalues.optaplanner.solution.cflp.domain.Customer",Arrays.stream(Customer.class.getDeclaredFields()).map(f->f.getName()).toList());
        clazz.put("cn.keyvalues.optaplanner.solution.cflp.domain.Assign",Arrays.stream(Assign.class.getDeclaredFields()).map(f->f.getName()).toList());
        return Result.OK(clazz);
    }

    @GetMapping("/childProperties")
    @Operation(summary = "获取类型可选属性")
    public Result<?> childProperties(String className){
        return Result.OK(Utils.childProperties(className));
    }

    @PostMapping("/defineConstraint")
    @Operation(summary = "定义一个约束")
    public Result<?> defineConstraint(@RequestBody ConstraintDefineVo define){
        // 只做保存就行，因为constraint需要constraintFactory,在使用的时候再根据定义生成就可以
        ConstraintDefinition definition = new ConstraintDefinition();
        definition.setConstraintDefinition(define);
        definition.setSolutionModel(getClass().getPackageName());
        boolean success=definitionService.save(definition);
        return success?Result.OK():Result.failed("");
    }

    @GetMapping("/listCustomeConstraint")
    @Operation(summary = "列出自定义约束列表")
    public Result<?> listCustomeConstraint(){
        return Result.OK(definitionService.list());
    }

    @DeleteMapping("/deleteCustomeConstraint")
    @Operation(summary = "删除自定义约束")
    public Result<?> deleteCustomeConstraint(String id){
        boolean success=definitionService.removeById(id);
        return success?Result.OK():Result.failed("");
    }

    /**************** CRUD ****************/
    
    @GetMapping("/listProblem")
    @Operation(summary = "问题列表")
    public Result<?> listProblem(){
        List<Map<String,Object>> listProblem = cflpService.listProblem();
        return Result.OK(listProblem);
    }

    @DeleteMapping("/deleteProblem")
    @Operation(summary = "删除问题")
    public Result<?> deleteProblem(String problemID){
        boolean success = cflpService.deleteProblem(UUID.fromString(problemID));
        return success?Result.OK():Result.failed("");
    }

}
