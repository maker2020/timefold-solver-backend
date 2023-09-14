package com.keyvalues.optaplanner.maprouting;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;

public class Main {
    public static void main(String[] args) {
        String configPath="optaplanner/maproutingSolverConfig.xml";
        SolverFactory<MapRoutingSolution> solverFactory = SolverFactory.createFromXmlResource(
                configPath);
        Solver<MapRoutingSolution> solver = solverFactory.buildSolver();
        MapRoutingSolution problem=new MapRoutingSolution();
        
        problem.setPointList(new ArrayList<>(){
            {
                // add(new Point(10,20));
                // add(new Point(0, 1));
                // add(new Point(0,8));
                // add(new Point(4,0));
                // add(new Point(8,8));
                add(new Point(0, 0)); //a
                add(new Point(0, 4)); //c
                add(new Point(2, 0)); //b
            }
        });

        List<Integer> orderRange=new ArrayList<>();
        for(int i=0;i<problem.getPointList().size();i++){
            orderRange.add(i);
        }
        problem.setOrderRange(orderRange);

        List<RoutingEntity> routing=new ArrayList<>();
        long id=0;
        routing.add(new RoutingEntity(id++));
        routing.add(new RoutingEntity(id++));
        routing.add(new RoutingEntity(id++));

        problem.setRouting(routing);

        MapRoutingSolution result = solver.solve(problem);
        System.out.println(result);
    }
}
