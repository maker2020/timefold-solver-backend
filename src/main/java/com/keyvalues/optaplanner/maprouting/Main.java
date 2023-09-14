package com.keyvalues.optaplanner.maprouting;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;

import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;

public class Main {

    public static List<List<Integer>> allSolutionOrder=new ArrayList<>();

    public static int count=0;
    
    public static void main(String[] args) {
        String configPath="optaplanner/maproutingSolverConfig.xml";
        SolverFactory<MapRoutingSolution> solverFactory = SolverFactory.createFromXmlResource(
                configPath);
        Solver<MapRoutingSolution> solver = solverFactory.buildSolver();
        MapRoutingSolution problem=new MapRoutingSolution();
        
        List<Point> pointList=new ArrayList<>(){
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
        };
        problem.setPointList(pointList);

        List<Integer> orderRange=new ArrayList<>();
        for(int i=0;i<problem.getPointList().size();i++){
            orderRange.add(i);
        }
        problem.setOrderRange(orderRange);

        List<RoutingEntity> routing=new ArrayList<>();
        long id=0;
        int index=0;
        routing.add(new RoutingEntity(id++,pointList.get(index++)));
        routing.add(new RoutingEntity(id++,pointList.get(index++)));
        routing.add(new RoutingEntity(id++,pointList.get(index++)));

        problem.setRouting(routing);

        solver.addEventListener(new SolverEventListener<MapRoutingSolution>() {
            @Override
            public void bestSolutionChanged(BestSolutionChangedEvent<MapRoutingSolution> event) {
                MapRoutingSolution newBestSolution = event.getNewBestSolution();
                List<RoutingEntity> newRouting = newBestSolution.getRouting();
                List<Integer> orderList=new ArrayList<>();
                for (int i = 0; i < newRouting.size(); i++) {
                    int order=newRouting.get(i).getOrder();
                    orderList.add(order);
                }
                allSolutionOrder.add(orderList);
            }
        });
        MapRoutingSolution result = solver.solve(problem);
        System.out.println(result);
        System.out.println("\n\n\n\n\n");
        System.out.println(allSolutionOrder);
        System.out.println("\n\n\n\n\n");
        // 由于各种约束交叉计算重复。提前算好组合分数.
        // System.out.println(count);
    }
}
