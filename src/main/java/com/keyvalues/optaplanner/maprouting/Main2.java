// package com.keyvalues.optaplanner.maprouting;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.optaplanner.core.api.solver.Solver;
// import org.optaplanner.core.api.solver.SolverFactory;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.keyvalues.optaplanner.geo.Point;
// import com.keyvalues.optaplanner.maprouting.controller.VisitorRoutingController;
// import com.keyvalues.optaplanner.maprouting.domain.Customer;
// import com.keyvalues.optaplanner.maprouting.domain.Location;
// import com.keyvalues.optaplanner.maprouting.domain.Visitor;
// import com.keyvalues.optaplanner.maprouting.domain.VisitorBase;
// import com.keyvalues.optaplanner.maprouting.domain.VisitorRoutingSolution;

// /**
//  * 验证成功，将(20,0)换成(18,0)，方案变为第二辆车直接去(18,0)。
//  */
// public class Main2 {
    
//     public static void main(String[] args) throws JsonProcessingException {
//         String configPath="optaplanner/visitorRoutingSolverConfig.xml";
//         SolverFactory<VisitorRoutingSolution> solverFactory = SolverFactory.createFromXmlResource(
//                 configPath);
//         Solver<VisitorRoutingSolution> solver = solverFactory.buildSolver();
//         VisitorRoutingSolution problem=new VisitorRoutingSolution();
//         long id__=0L;
//         List<Location> locationList = new ArrayList<>();
//         // 起点不放客户列表
//         // locationList.add(new Location(id__++,new Point(0, 0)));
//         locationList.add(new Location(id__++,new Point(0, 8)));
//         locationList.add(new Location(id__++,new Point(9, 15)));
//         locationList.add(new Location(id__++,new Point(18, 0)));
//         locationList.add(new Location(id__++,new Point(20, 20)));
//         // 所有点位
//         problem.setLocationList(locationList);
//         // 客户列表(同点位列表)
//         List<Customer> customers=new ArrayList<>();
//         Long id_=0L;
//         for(Location location:locationList){
//             Customer customer=new Customer();
//             customer.setId(id_++);
//             customer.setLocation(location);
//             customers.add(customer);
//         }
//         problem.setCustomerList(customers);
//         // 起点（1个）
//         List<VisitorBase> visitorBases=new ArrayList<>(){{
//             // locationList.get(0))
//             // 引用还是new?都无所谓
//             add(new VisitorBase(0,new Location(99,new Point(0, 0))));
//         }};
//         problem.setVisitorBases(visitorBases);
//         // 访问者列表(1,2位)
//         Long id=0L;
//         List<Visitor> visitors=new ArrayList<>();
//         visitors.add(new Visitor(id++,visitorBases.get(0)));
//         visitors.add(new Visitor(id++,visitorBases.get(0)));
//         problem.setVisitorList(visitors);

//         // 生成p2p优化图还需要把基地算进去
//         locationList.add(visitorBases.get(0).getLocation());
//         List<List<Point>> combinePoint = combinePoint(locationList.stream().map((o)->o.getPoint()).toList(), 2);
//         // 再移除
//         locationList.remove(locationList.size()-1);
//         Map<String,Long> map = createOptimalValueMapMap(combinePoint);
//         VisitorRoutingController.p2pOptimalValueMap.putAll(map);
//         VisitorRoutingSolution solution = solver.solve(problem);
        
//         ObjectMapper objectMapper=new ObjectMapper();
//         String json = objectMapper.writeValueAsString(solution);
//         System.out.println(json);

//     }

//     private static Map<String,Long> createOptimalValueMapMap(List<List<Point>> p2pList){
//         Map<String,Long> p2pOptimalValueMap=new HashMap<>();
//         for (List<Point> point : p2pList) {
//             Point a=point.get(0);
//             Point b=point.get(1);
//             StringBuilder sb=new StringBuilder();
//             // a->b
//             long optimalValue0 = calculate(a, b);
//             String key0=sb.append(a.toString()).append("->").append(b.toString()).toString();
//             p2pOptimalValueMap.put(key0, optimalValue0);
//             sb.setLength(0);
//             // b->a
//             long optimalValue1 = calculate(a, b);
//             String key1=sb.append(b.toString()).append("->").append(a.toString()).toString();
//             p2pOptimalValueMap.put(key1, optimalValue1);
//         }
//         return p2pOptimalValueMap;
//     }

//     private static long calculate(Point p1,Point p2){
//         double latitude = p1.getLatitude();
//         double longitude = p1.getLongitude();
//         double latitude2 = p2.getLatitude();
//         double longitude2 = p2.getLongitude();
//         double deltaX=longitude2-longitude;
//         double deltaY=latitude2-latitude;
//         double distance=Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//         return (long)(distance*1000);
//     }

//     private static List<List<Point>> combinePoint(List<Point> points,int choose) {
//         List<List<Point>> combinationList = new ArrayList<>();
//         class Combine{
//             static void dfs(List<Point> elements, int choose, int start, List<Point> current, List<List<Point>> combinationCollection) {
//                 if (choose == 0) {
//                     combinationCollection.add(new ArrayList<>(current));
//                     return;
//                 }
//                 for (int i = start; i < elements.size(); i++) {
//                     current.add(elements.get(i));
//                     dfs(elements, choose - 1, i + 1, current, combinationCollection);
//                     current.remove(current.size() - 1);
//                 }
//             }
//         }
//         Combine.dfs(points, choose, 0, new ArrayList<>(), combinationList);
//         return combinationList;
//     }
// }
