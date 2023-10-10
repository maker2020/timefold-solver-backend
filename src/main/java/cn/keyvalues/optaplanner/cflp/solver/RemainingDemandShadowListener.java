package cn.keyvalues.optaplanner.cflp.solver;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.cflp.domain.Customer;
import cn.keyvalues.optaplanner.cflp.domain.FacilityLocationSolution;

public class RemainingDemandShadowListener implements VariableListener<FacilityLocationSolution,Customer>{

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
    public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        // ServerStation serverStation = entity.getServerStation();
        // if(serverStation==null) return;
        // long remainingDemand = entity.getRemainingDemand()-serverStation.getCapacity();
        // remainingDemand=remainingDemand<0?0:remainingDemand;
        // scoreDirector.beforeVariableChanged(entity, "remainingDemand");
        // entity.setRemainingDemand(remainingDemand);
        // scoreDirector.afterVariableChanged(entity, "remainingDemand");
    }
    
}
