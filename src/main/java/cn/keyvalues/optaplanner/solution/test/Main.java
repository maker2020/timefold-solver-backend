package cn.keyvalues.optaplanner.solution.test;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import com.alibaba.fastjson.JSON;

import cn.keyvalues.optaplanner.geo.Point;
import cn.keyvalues.optaplanner.solution.test.domain.Customer;
import cn.keyvalues.optaplanner.solution.test.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.test.domain.Location;
import cn.keyvalues.optaplanner.solution.test.domain.ServerStation;

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


        String configPath="optaplanner/test/facilityLocationSolverConfig.xml";
        SolverFactory<FacilityLocationSolution> solverFactory = SolverFactory.createFromXmlResource(
                configPath);
        Solver<FacilityLocationSolution> solver = solverFactory.buildSolver();
        FacilityLocationSolution resolved = solver.solve(solution);
        System.out.println(JSON.toJSONString(resolved));

    }
}
