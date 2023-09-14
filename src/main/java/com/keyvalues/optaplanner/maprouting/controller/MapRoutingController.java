package com.keyvalues.optaplanner.maprouting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keyvalues.optaplanner.common.Result;
import com.keyvalues.optaplanner.maprouting.controller.vo.PointInputVo;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.service.SolverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "线路规划")
@RestController
@RequestMapping("/maprouting")
public class MapRoutingController {

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

}
