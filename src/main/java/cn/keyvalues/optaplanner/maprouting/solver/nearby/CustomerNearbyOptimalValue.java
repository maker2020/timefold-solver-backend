package cn.keyvalues.optaplanner.maprouting.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

import cn.keyvalues.optaplanner.maprouting.domain.Customer;

public class CustomerNearbyOptimalValue implements NearbyDistanceMeter<Customer, Customer>{

    @Override
    public double getNearbyDistance(Customer origin, Customer destination) {
        return origin.getOptimalValueTo(destination);
        // If arriving early also inflicts a cost (more than just not using the vehicle more), such as the driver's wage, use this:
        //        if (origin instanceof TimeWindowedCustomer && destination instanceof TimeWindowedCustomer) {
        //            distance += ((TimeWindowedCustomer) origin).getTimeWindowGapTo((TimeWindowedCustomer) destination);
        //        }
        // return distance;
    }

}