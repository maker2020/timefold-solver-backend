package cn.keyvalues.optaplanner.solution.maprouting.solver.nearby;

import ai.timefold.solver.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

import cn.keyvalues.optaplanner.solution.maprouting.domain.Customer;
import cn.keyvalues.optaplanner.solution.maprouting.domain.LocationAware;

public class CustomerNearbyOptimalValue implements NearbyDistanceMeter<Customer, LocationAware>{

    /**
     * 用于不同邻里选择器获得距离的接口
     * @param origin 如果是list move seletor,它总为LIST中元素类型
     * @param destination
     * @return
     */
    @Override
    public double getNearbyDistance(Customer origin, LocationAware destination) {
        return origin.getOptimalValueTo(destination.getLocation());
        // If arriving early also inflicts a cost (more than just not using the vehicle more), such as the driver's wage, use this:
        //        if (origin instanceof TimeWindowedCustomer && destination instanceof TimeWindowedCustomer) {
        //            distance += ((TimeWindowedCustomer) origin).getTimeWindowGapTo((TimeWindowedCustomer) destination);
        //        }
        // return distance;
    }

}