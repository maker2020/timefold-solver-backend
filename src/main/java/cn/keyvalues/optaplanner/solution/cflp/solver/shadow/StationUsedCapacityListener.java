package cn.keyvalues.optaplanner.solution.cflp.solver.shadow;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;
import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;

public class StationUsedCapacityListener implements VariableListener<FacilityLocationSolution,Customer>{

    @Override
    public void beforeEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        
   }

    @Override
    public void afterEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
       
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
       
    }

    @Override
    public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer customer) {
        if(customer!=null){
            for (Assign assignStations : customer.getAssignedStations()) {
                ServerStation station = assignStations.getStation();
                if(station!=null){
                    long usedCapacity=station.getAssignedCustomers().stream().filter(assign->assign.getCustomer()!=null 
                                            && assign.getAssignedDemand()!=null)
                                            .mapToLong(assign->assign.getAssignedDemand()).sum();
                    scoreDirector.beforeVariableChanged(station, "usedCapacity");
                    station.setUsedCapacity(usedCapacity);
                    scoreDirector.afterVariableChanged(station, "usedCapacity");
                }
            }
        }
    }
    

    // @Hidden
    // public long getUsedCapacity(){
    //     // 两个含义：分配即使用了；分配且利用即使用了。目前是前者
    //     return assignedCustomers.stream().filter(assign->assign.getCustomer()!=null 
    //             && assign.getAssignedDemand()!=null)
    //             .mapToLong(assign->assign.getAssignedDemand()).sum();
    // }
}
