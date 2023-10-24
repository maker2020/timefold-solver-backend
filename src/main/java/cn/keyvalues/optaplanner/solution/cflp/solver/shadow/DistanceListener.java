package cn.keyvalues.optaplanner.solution.cflp.solver.shadow;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;
import cn.keyvalues.optaplanner.solution.cflp.domain.ServerStation;

public class DistanceListener implements VariableListener<FacilityLocationSolution,Assign>{

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
        if(entity.getCustomer()!=null && entity.getStation()!=null){
            Customer c=entity.getCustomer();
            ServerStation s=entity.getStation();
            if(c.getLocation()!=null && s.getLocation()!=null){
                double distance=c.getLocation().getDistanceTo(s.getLocation());
                scoreDirector.beforeVariableChanged(entity, "betweenDistance");
                entity.setBetweenDistance(distance);
                scoreDirector.afterVariableChanged(entity, "betweenDistance");
            }
        }else{
            scoreDirector.beforeVariableChanged(entity, "betweenDistance");
            entity.setBetweenDistance(null);
            scoreDirector.afterVariableChanged(entity, "betweenDistance");
        }
    }

    // public double getBetweenDistance(){
    //     if(customer!=null && customer.getLocation()!=null
    //             && station!=null && station.getLocation()!=null){
    //         return customer.getLocation().getDistanceTo(station.getLocation());
    //     }
    //     return -1;
    // }
    
}
