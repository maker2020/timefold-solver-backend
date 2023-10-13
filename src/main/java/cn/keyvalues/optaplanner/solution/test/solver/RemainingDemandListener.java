package cn.keyvalues.optaplanner.solution.test.solver;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.test.domain.Customer;
import cn.keyvalues.optaplanner.solution.test.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.test.domain.ServerStation;

/**
 * 在Customer中，CustomeredDemand被分配/被改变后，更新remaing的值
 */
public class RemainingDemandListener implements VariableListener<FacilityLocationSolution,Customer>{

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

    // 测试shadow var可以，但方式要注意,避免计数错误
    @Override
    public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        if(entity==null || entity.getStation()==null){
            return;
        }
        ServerStation station = entity.getStation();
        long remainingDemand=entity.getMaxDemand()-station.getMaxCapacity();
        remainingDemand=remainingDemand<0?0:remainingDemand;
        scoreDirector.beforeVariableChanged(entity, "remainingDemand");
        entity.setRemainingDemand(remainingDemand);
        scoreDirector.afterVariableChanged(entity, "remainingDemand");
    }
    
}
