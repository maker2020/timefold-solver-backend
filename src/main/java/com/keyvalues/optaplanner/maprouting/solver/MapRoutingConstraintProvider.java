package com.keyvalues.optaplanner.maprouting.solver;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import com.keyvalues.optaplanner.maprouting.domain.RoutingEntity;


public class MapRoutingConstraintProvider implements ConstraintProvider{

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            // visitConstraint(constraintFactory),
            orderConstraint(constraintFactory),
            calculateTotalDistanceConstraint(constraintFactory),
            startingEntityOrderConstraint(constraintFactory),
            endingEntityOrderConstraint(constraintFactory)
            // testConstraint(constraintFactory)
        };
    }

    // private Constraint testConstraint(ConstraintFactory factory) {
    //     return factory.forEach(RoutingEntity.class)
    //         .filter(route->route.getVisitPoint().equals(point) && route.getOrder()==0)
    //         .penalize(HardSoftScore.ONE_SOFT,r->999)
    //         .asConstraint("Test Constraint");
    // }

    // /**
    //  * 所有点必须全部访问且不重复
    //  */
    // private Constraint visitConstraint(ConstraintFactory factory) {
    //     return factory.forEach(RoutingEntity.class)
    //         .join(RoutingEntity.class,Joiners.lessThan(RoutingEntity::getId),Joiners.equal(RoutingEntity::getVisitPoint))
    //         .penalize(HardSoftScore.ONE_HARD)
    //         .asConstraint("Visit All");
    // }

    /**
     * 顺序不重复约束
     */
    private Constraint orderConstraint(ConstraintFactory factory){
        return factory.forEach(RoutingEntity.class)
            .join(RoutingEntity.class,Joiners.lessThan(RoutingEntity::getId),Joiners.equal(RoutingEntity::getOrder))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Order Range");
    }

    private Constraint startingEntityOrderConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RoutingEntity.class)
                .filter(entity -> entity.isStart() && entity.getOrder() != 0)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Starting Order Must be 0");
    }

    private Constraint endingEntityOrderConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RoutingEntity.class)
                .filter(entity -> entity.isEnd() && entity.getOrder() != entity.getTotalPointsNum()-1)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Ending Order Must be last index");
    }

    /**
     * 总路程最短软约束
     */
    private Constraint calculateTotalDistanceConstraint(ConstraintFactory factory) {
        return factory.forEach(RoutingEntity.class)
            .join(RoutingEntity.class)
            .filter((r1, r2) -> r1.getOrder() + 1 == r2.getOrder())
            .penalize(HardSoftScore.ONE_SOFT,(r1,r2)->RoutingEntity.getApiDistance(r1, r2))
            .asConstraint("MIN DISTANCE");
    }

    // public static void main(String[] args) {
    //     String configPath="optaplanner/maproutingSolverConfig.xml";
    //     SolverFactory<MapRoutingSolution> solverFactory = SolverFactory.createFromXmlResource(
    //             configPath);

    //     SolutionManager<MapRoutingSolution,HardSoftScore> solutionManager=SolutionManager.create(solverFactory);

    //     MapRoutingSolution solution=generateProblem();
    //     HardSoftScore score=solutionManager.update(solution);
    //     System.out.println(score);
    // }

    // private static MapRoutingSolution generateProblem(){
    //     MapRoutingSolution problem=new MapRoutingSolution();
    //     problem.setPointList(new ArrayList<>(){
    //         {
    //             add(new Point(0, 0)); //a
    //             add(new Point(0, 4)); //c
    //             add(new Point(2, 0)); //b
    //         }
    //     });
    //     List<Integer> orderRange=new ArrayList<>();
    //     for(int i=0;i<problem.getPointList().size();i++){
    //         orderRange.add(i);
    //     }
    //     problem.setOrderRange(orderRange);

    //     List<RoutingEntity> routing=new ArrayList<>();
    //     long id=0;
    //     routing.add(new RoutingEntity(id++,new Point(0, 4),2));
    //     routing.add(new RoutingEntity(id++,new Point(0, 0),0));
    //     routing.add(new RoutingEntity(id++,new Point(2, 0),1));

    //     problem.setRouting(routing);
    //     return problem;
    // }

}
