package com.keyvalues.optaplanner.maprouting.solver;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import com.keyvalues.optaplanner.maprouting.domain.Customer;

public class VisitorRoutingConstraint implements ConstraintProvider{

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            distanceToPreviousStandstill(constraintFactory),
            // distanceFromLastCustomerToDepot(constraintFactory),
        };
    }

    /**
     * 链式计算总路径。 （上一个为null则算到起点到距离。）
     */
    protected Constraint distanceToPreviousStandstill(ConstraintFactory factory) {
        return factory.forEach(Customer.class)
                .filter(customer -> customer.getVisitor() != null)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Customer::getOptimalValueFromPreviousStandstill)
                .asConstraint("distanceToPreviousStandstill");
    }

    /**
     * 单独计算的：最后一个客户点到原点的距离
     */
    protected Constraint distanceFromLastCustomerToDepot(ConstraintFactory factory) {
        return factory.forEach(Customer.class)
                .filter(customer -> customer.getVisitor() != null && customer.getNextCustomer() == null)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Customer::getOptimalValueToDepot)
                .asConstraint("distanceFromLastCustomerToDepot");
    }
    
}
