package cn.keyvalues.optaplanner.solution.cflp.solver;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;

/**
 * 在Assign中，assignedDemand被分配/被改变后，更新remaing的值
 */
public class RemainingDemandListener implements VariableListener<FacilityLocationSolution,Assign>{

    @Override
    public void beforeEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {

    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
       
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
     
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
       
    }

    @Override
    public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Assign entity) {
        if(entity==null || entity.getCustomer()==null){
            return;
        }
        long assignedDemand = entity.getAssignedDemand();
        Customer customer = entity.getCustomer();
        scoreDirector.beforeVariableChanged(customer, "remainingDemand");
        customer.setRemainingDemand(customer.getMaxDemand()-assignedDemand);
        scoreDirector.afterVariableChanged(customer, "remainingDemand");
    }
    
}
