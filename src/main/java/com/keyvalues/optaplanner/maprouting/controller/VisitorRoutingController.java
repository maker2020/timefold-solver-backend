package com.keyvalues.optaplanner.maprouting.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "组合TSP问题的线路规划")
@RestController
@RequestMapping("/vistorRouting")
public class VisitorRoutingController {
  
    public static final Map<String,Long> p2pOptimalValueMap=new ConcurrentHashMap<>();

}
