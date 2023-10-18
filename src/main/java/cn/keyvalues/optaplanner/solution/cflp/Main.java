package cn.keyvalues.optaplanner.solution.cflp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import com.alibaba.fastjson.JSON;

import cn.keyvalues.optaplanner.geo.Point;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.cflp.domain.Location;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

public class Main {
    public static void main(String[] args) {
        FacilityLocationSolution solution=new FacilityLocationSolution();
        // customers
        List<Customer> customers=new ArrayList<>();
        customers.add(new Customer(1, 10, new Location(1, new Point(10, 0))));
        customers.add(new Customer(2, 5, new Location(2, new Point(20, 0))));
        customers.add(new Customer(3, 10, new Location(3, new Point(30, 0))));
        customers.add(new Customer(4, 10, new Location(4, new Point(40, 0))));
        
        // customers.add(new Customer(1, 25, new Location(1,new Point(10, 0))));

        solution.setCustomers(customers);

        // stations
        List<ServerStation> stations=new ArrayList<>();
        stations.add(new ServerStation(0, new Location(0, new Point(0, 0)), 20, 35));
        stations.add(new ServerStation(5, new Location(5, new Point(50, 0)), 20, 35));

        // // 可被优化删减的服务站
        stations.add(new ServerStation(99, new Location(99, new Point(25, 0)), 20, 100));
        
        // stations.add(new ServerStation(0, new Location(0, new Point(0, 0)), 20, 15));
        // stations.add(new ServerStation(2, new Location(2, new Point(20, 0)), 20, 15));

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
        List<Assign> assigns=new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Assign assign = new Assign(i);
            // Initialize other properties of Assign if needed
            assigns.add(assign);
        }

        solution.setAssigns(assigns);

        String configPath="optaplanner/facilityLocationSolverConfig.xml";
        SolverFactory<FacilityLocationSolution> solverFactory = SolverFactory.createFromXmlResource(
                configPath);
        Solver<FacilityLocationSolution> solver = solverFactory.buildSolver();
        FacilityLocationSolution resolved = solver.solve(solution);
        System.out.println(JSON.toJSONString(resolved));

    }
}
