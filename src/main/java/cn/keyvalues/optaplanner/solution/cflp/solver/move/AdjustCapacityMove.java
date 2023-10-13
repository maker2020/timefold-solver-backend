// package cn.keyvalues.optaplanner.solution.cflp.solver.move;

// import java.util.Collection;
// import java.util.Collections;

// import org.optaplanner.core.api.score.director.ScoreDirector;
// import org.optaplanner.core.impl.heuristic.move.AbstractMove;

// import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
// import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
// import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

// public class AdjustCapacityMove extends AbstractMove<FacilityLocationSolution>{

//     private Customer customer;
//     private ServerStation serverStation;

//     public AdjustCapacityMove(Customer customer,ServerStation serverStation){
//         this.customer=customer;
//         this.serverStation=serverStation;
//     }

//     @Override
//     public boolean isMoveDoable(ScoreDirector<FacilityLocationSolution> scoreDirector) {
//         // 判断条件写这里？
//         return customer.isAssigned() && serverStation.isUsed();
//     }

//     @Override
//     protected AbstractMove<FacilityLocationSolution> createUndoMove(
//             ScoreDirector<FacilityLocationSolution> scoreDirector) {
//                 // 
//                 // do....
//         return new AdjustCapacityMove(customer, serverStation);
//     }

//     // 多线程增量求解必须重写
//     // @Override
//     // public AdjustCapacityMove rebase(ScoreDirector<FacilityLocationSolution> destinationScoreDirector) {
//     //     return new AdjustCapacityMove(destinationScoreDirector.lookUpWorkingObject(customer),
//     //             destinationScoreDirector.lookUpWorkingObject(serverStation));
//     // }

//     @Override
//     protected void doMoveOnGenuineVariables(ScoreDirector<FacilityLocationSolution> scoreDirector) {
//         long demand = customer.getDemand();
//         long capacity = serverStation.getCapacity();

//         scoreDirector.beforeProblemPropertyChanged(customer);
//         scoreDirector.beforeProblemPropertyChanged(serverStation);
//         if (demand > capacity) {
//             // Adjust customer demand and service station capacity
//             customer.setDemand(demand - capacity);
//             serverStation.setCapacity(0);
//         }
//         scoreDirector.afterProblemPropertyChanged(customer);
//         scoreDirector.afterProblemPropertyChanged(serverStation);
//     }

//     @Override
//     public Collection<? extends Object> getPlanningEntities() {
//         return Collections.singletonList(customer);
//     }

//     @Override
//     public Collection<? extends Object> getPlanningValues() {
//         return Collections.singletonList(serverStation);
//     }

// }
