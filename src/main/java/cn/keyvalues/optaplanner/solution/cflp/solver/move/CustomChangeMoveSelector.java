// package cn.keyvalues.optaplanner.solution.cflp.solver.move;

// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;

// import org.optaplanner.core.api.score.director.ScoreDirector;
// import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
// import org.optaplanner.core.impl.heuristic.move.Move;
// import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
// import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
// import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
// import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelector;
// import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
// import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
// import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
// import org.optaplanner.core.impl.solver.scope.SolverScope;

// import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
// import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
// import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

// public class CustomChangeMoveSelector extends ChangeMoveSelector<FacilityLocationSolution> {

//     public CustomChangeMoveSelector(EntitySelector<FacilityLocationSolution> entitySelector,
//             ValueSelector<FacilityLocationSolution> valueSelector, boolean randomSelection) {
//         super(entitySelector, valueSelector, randomSelection);
//     }

//     public Iterator<Move<FacilityLocationSolution>> iterator() {
//         List<Move<FacilityLocationSolution>> moves = new ArrayList<>();

//         for (Customer customer : customers) {
//             if (customer.isAssigned()) {
//                 ServiceStation serviceStation = customer.getServiceStation();
//                 if (serviceStation != null) {
//                     int demand = customer.getDemand();
//                     int capacity = serviceStation.getCapacity();

//                     if (demand > capacity) {
//                         // Adjust customer demand and service station capacity
//                         int adjustedDemand = demand - capacity;
//                         int adjustedCapacity = 0;

//                         // Create moves to adjust capacity
//                         Move<FacilityLocationSolution> customerMove = new GenericChangeMove<>(customer, "demand", adjustedDemand);
//                         Move<FacilityLocationSolution> stationMove = new GenericChangeMove<>(serviceStation, "capacity", adjustedCapacity);

//                         moves.add(customerMove);
//                         moves.add(stationMove);
//                     }
//                 }
//             }
//         }

//         return moves.iterator();
//     }


// }
