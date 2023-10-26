package cn.keyvalues.optaplanner.solution.cflp.solver.shadow;

import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import cn.keyvalues.optaplanner.solution.cflp.domain.Assign;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
import cn.keyvalues.optaplanner.solution.cflp.domain.FacilityLocationSolution;

public class RemainingDemandListener implements VariableListener<FacilityLocationSolution,Customer>{

    @Override
    public void beforeEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        
    }

    @Override
    public void afterEntityAdded(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        updateRemainingDemand(scoreDirector, entity);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
       
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        // above methods won't be called in shadow var listener
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer entity) {
        
    }

    @Override
    public void afterVariableChanged(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer customer) {
        updateRemainingDemand(scoreDirector, customer);
    }

    private void updateRemainingDemand(ScoreDirector<FacilityLocationSolution> scoreDirector, Customer customer){
        // if(customer==null){
        //     return;
        // }
        
        long remainingDemand=customer.getMaxDemand();
        for (Assign assign : customer.getAssignedStations()) {
            if(assign.getStation()!=null && assign.getAssignedDemand()!=null){
                remainingDemand-=assign.getAssignedDemand();
            }
        }
        remainingDemand=remainingDemand<0?0:remainingDemand;
        scoreDirector.beforeVariableChanged(customer, "remainingDemand");
        customer.setRemainingDemand(remainingDemand);
        scoreDirector.afterVariableChanged(customer, "remainingDemand");
        
    }

    // 记录：之前的普通实现。此处用影子变量才刷新分数，或者说影子变量适合参与约束计算
    // 考虑一个变量和分数有关时，先考虑是否和规划变量有依赖关联性等。
    // @Hidden
    // public long getRemainingDemand(){
    //     long remainingDemand=maxDemand;
    //     for (Assign assign : assignedStations) {
    //         // 无效代码:assign.getCustomer()!=null && assign.getCustomer()==this 
    //         if(assign.getStation()!=null && assign.getAssignedDemand()!=null){
    //             remainingDemand-=assign.getAssignedDemand();
    //         }
    //     }
    //     return remainingDemand<0?0:remainingDemand;
    // }
    
}
