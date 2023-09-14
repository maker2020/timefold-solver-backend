package com.keyvalues.optaplanner.maprouting.solver;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolverFactory;

import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.maprouting.domain.MapRoutingSolution;
import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;
import com.keyvalues.optaplanner.maprouting.utils.RoutingUtil;


public class MapRoutingConstraintProvider implements ConstraintProvider{

    static Point point=new Point(0, 0);

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            visitConstraint(constraintFactory),
            orderConstraint(constraintFactory),
            calculateTotalDistanceConstraint(constraintFactory),
            // testConstraint(constraintFactory)
        };
    }

    // private Constraint testConstraint(ConstraintFactory factory) {
    //     return factory.forEach(RoutingEntity.class)
    //         .filter(route->route.getVisitPoint().equals(point) && route.getOrder()==0)
    //         .penalize(HardSoftScore.ONE_SOFT,r->999)
    //         .asConstraint("Test Constraint");
    // }

    /**
     * 所有点必须全部访问且不重复
     */
    private Constraint visitConstraint(ConstraintFactory factory) {
        return factory.forEach(RoutingEntity.class)
            .join(RoutingEntity.class,Joiners.lessThan(RoutingEntity::getId),Joiners.equal(RoutingEntity::getVisitPoint))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Visit All");
    }

    /**
     * 顺序不重复约束
     */
    private Constraint orderConstraint(ConstraintFactory factory){
        return factory.forEach(RoutingEntity.class)
            .join(RoutingEntity.class,Joiners.lessThan(RoutingEntity::getId),Joiners.equal(RoutingEntity::getOrder))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Order Range");
    }

    // 或 C->A->B 违反了硬约束
    // 或 C->A->B 算路径多算了 （已排除）

    /**
     * 总路程最短软约束
     */
    private Constraint calculateTotalDistanceConstraint(ConstraintFactory factory) {
        return factory.forEach(RoutingEntity.class)
            .join(RoutingEntity.class)
            .filter((r1, r2) -> r1.getOrder() + 1 == r2.getOrder())
            .penalize(HardSoftScore.ONE_SOFT,(r1,r2)->RoutingUtil.calculateDistance(r1, r2))
            .asConstraint("MIN DISTANCE");
    }

    public static void main(String[] args) {
        String configPath="optaplanner/maproutingSolverConfig.xml";
        SolverFactory<MapRoutingSolution> solverFactory = SolverFactory.createFromXmlResource(
                configPath);

        SolutionManager<MapRoutingSolution,HardSoftScore> solutionManager=SolutionManager.create(solverFactory);

        MapRoutingSolution solution=generateProblem();
        HardSoftScore score=solutionManager.update(solution);
        System.out.println(score);
    }

    private static MapRoutingSolution generateProblem(){
        MapRoutingSolution problem=new MapRoutingSolution();
        problem.setPointList(new ArrayList<>(){
            {
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
        routing.add(new RoutingEntity(id++,new Point(0, 4),2));
        routing.add(new RoutingEntity(id++,new Point(0, 0),0));
        routing.add(new RoutingEntity(id++,new Point(2, 0),1));

        problem.setRouting(routing);
        return problem;
    }

}
