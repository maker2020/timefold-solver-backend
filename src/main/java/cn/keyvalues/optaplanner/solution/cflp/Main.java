package cn.keyvalues.optaplanner.solution.cflp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;

import com.alibaba.fastjson.JSON;

import cn.keyvalues.optaplanner.common.geo.Point;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.cflp.domain.Location;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

public class Main {
    public static void main(String[] args) {
        test(null);
    }

    public static void test(FacilityLocationSolution initializedSolution){
        FacilityLocationSolution solution;
        if(initializedSolution==null){
            solution=new FacilityLocationSolution();
            // customers
            List<Customer> customers=new ArrayList<>();
            // customers.add(new Customer(1, 10, new Location(1, new Point(10, 0))));
            // customers.add(new Customer(2, 5, new Location(2, new Point(20, 0))));
            // customers.add(new Customer(3, 10, new Location(3, new Point(30, 0))));
            // customers.add(new Customer(4, 10, new Location(4, new Point(40, 0))));
            
            customers.add(new Customer(1, 20, new Location(1,new Point(10, 0)),3));

            solution.setCustomers(customers);

            // stations
            List<ServerStation> stations=new ArrayList<>();
            // stations.add(new ServerStation(0, new Location(0, new Point(0, 0)), 20, 35));
            // stations.add(new ServerStation(5, new Location(5, new Point(50, 0)), 20, 35));

            // // // 可被优化删减的服务站
            // stations.add(new ServerStation(99, new Location(99, new Point(25, 0)), 20, 100));
            
            stations.add(new ServerStation(0, new Location(0, new Point(0, 0)), 20, 15,4));
            stations.add(new ServerStation(2, new Location(2, new Point(12, 0)), 20, 15,3));

            solution.setServerStations(stations);

            // demand choices range
            // 按客户最大容量，求解器有机会拆分
            long demand=Collections.max(customers, (r1,r2)->Long.compare(r1.getMaxDemand(), r2.getMaxDemand())).getMaxDemand();
            List<Long> demandChoices=new ArrayList<>();
            for(int i=1;i<=demand;i++){ // 不能分配0个，因为属于不分配
                demandChoices.add((long)i);
            }

            solution.setDemandChoices(demandChoices);

            // Assign的结果数量怎么去定，还需要研究
            // 结果未知只能按最大区限，所以规划变量nullable=true
            // 即 m * n
            List<Assign> assigns=new ArrayList<>();
            for (int i = 0; i < solution.getCustomers().size()*solution.getServerStations().size(); i++) {
                Assign assign = new Assign(i);
                // Initialize other properties of Assign if needed
                assigns.add(assign);
            }

            solution.setAssigns(assigns);

            // System.out.println(JSON.toJSONString(solution.getCustomers()));
            // System.out.println(JSON.toJSONString(solution.getServerStations()));
        }else{
            solution=initializedSolution;
        }
        
        String configPath="optaplanner/facilityLocationSolverConfig.xml";
        SolverFactory<FacilityLocationSolution> solverFactory = SolverFactory.createFromXmlResource(
                configPath);
        SolutionManager<FacilityLocationSolution,HardMediumSoftLongScore> solutionManager=SolutionManager.create(solverFactory);
        SolverManager<FacilityLocationSolution,Long> solverManager=SolverManager.create(solverFactory);
        List<FacilityLocationSolution> list=new ArrayList<>();
        SolverJob<FacilityLocationSolution,Long> solverJob=solverManager.solveAndListen(66L, (s)->solution, (update)->{
            list.add(update);
        },(finalRes)->{
            System.out.println("impossible:"+solverManager.getSolverStatus(66L));
            list.add(finalRes);
        },(id,ex)->{
            System.out.println("eeeeee rrrrr  rrrrr ooooo  rrrr");
        });
        try {
            FacilityLocationSolution resultSolution=solverJob.getFinalBestSolution();
            // System.out.println(list);
            System.out.println("done ... ");
            solutionManager.update(resultSolution);
            // System.out.println(JSON.toJSONString(resolved));
            var obj=solutionManager.explain(resultSolution);
            System.out.println(obj);
            System.out.println(JSON.toJSONString(resultSolution));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
