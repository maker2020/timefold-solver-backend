package cn.keyvalues.optaplanner.solution.test.solver;

import java.util.List;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.test.domain.ServerStation;
import cn.keyvalues.optaplanner.solution.test.domain.Customer;
import cn.keyvalues.optaplanner.solution.test.domain.FacilityLocationSolution;

public class RemainingCapacityListener implements VariableListener<FacilityLocationSolution,ServerStation>{

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
        
        List<Customer> assignedCustomers = station.getAssignedCustomers();
        long totalDemand=0;
        for (Customer customer : assignedCustomers) {
            totalDemand+=customer.getMaxDemand();
        }
        long remainingCapacity=station.getMaxCapacity()-totalDemand;
        remainingCapacity=remainingCapacity<0?0:remainingCapacity;
        scoreDirector.beforeVariableChanged(station, "remainingCapacity");
        station.setRemainingCapacity(remainingCapacity);
        scoreDirector.afterVariableChanged(station, "remainingCapacity");

    }
    
}
