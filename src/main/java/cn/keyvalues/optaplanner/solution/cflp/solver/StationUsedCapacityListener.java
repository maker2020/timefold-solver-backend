package cn.keyvalues.optaplanner.solution.cflp.solver;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;

public class StationUsedCapacityListener implements VariableListener<FacilityLocationSolution,ServerStation>{

    @Override
    public void beforeEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, ServerStation entity) {
        
   }

    @Override
    public void afterEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, ServerStation entity) {
        
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, ServerStation entity) {
        
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, ServerStation entity) {
       
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, ServerStation entity) {
       
    }

    @Override
    public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, ServerStation station) {
        if(station==null) return;
        long usedCapacity=station.getAssignedCustomers().stream().filter(assign->assign.getCustomer()!=null 
                && assign.getAssignedDemand()!=null)
                .mapToLong(assign->assign.getAssignedDemand()).sum();
        scoreDirector.beforeVariableChanged(station, "usedCapacity");
        station.setUsedCapacity(usedCapacity);
        scoreDirector.afterVariableChanged(station, "usedCapacity");

    }
    

    // @Hidden
    // public long getUsedCapacity(){
    //     // 两个含义：分配即使用了；分配且利用即使用了。目前是前者
    //     return assignedCustomers.stream().filter(assign->assign.getCustomer()!=null 
    //             && assign.getAssignedDemand()!=null)
    //             .mapToLong(assign->assign.getAssignedDemand()).sum();
    // }
}
