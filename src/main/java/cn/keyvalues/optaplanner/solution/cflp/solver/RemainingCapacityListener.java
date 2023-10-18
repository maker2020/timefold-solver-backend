// package cn.keyvalues.optaplanner.solution.cflp.solver;

// import org.optaplanner.core.api.domain.variable.VariableListener;
// import org.optaplanner.core.api.score.director.ScoreDirector;

// import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
// import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
// import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

// public class RemainingCapacityListener implements VariableListener<FacilityLocationSolution,Assign>{

//     @Override
//     public void beforeEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
        
//    }

//     @Override
//     public void afterEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
        
//     }

//     @Override
//     public void beforeEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
        
//     }

//     @Override
//     public void afterEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
       
//     }

//     @Override
//     public void beforeVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
       
//     }

//     @Override
//     public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
//         if(entity==null || entity.getStation()==null){
//             return;
//         }
//         long assignedDemand = entity.getAssignedDemand();
//         ServerStation station = entity.getStation();
//         scoreDirector.beforeVariableChanged(station, "remainingCapacity");
//         station.setRemainingCapacity(station.getMaxCapacity()-assignedDemand);
//         scoreDirector.afterVariableChanged(station, "remainingCapacity");

//     }
    
// }
