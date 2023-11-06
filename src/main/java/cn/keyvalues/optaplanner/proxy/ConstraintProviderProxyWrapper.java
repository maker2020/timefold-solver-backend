package cn.keyvalues.optaplanner.proxy;

import java.lang.reflect.Proxy;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import cn.keyvalues.optaplanner.solution.cflp.solver.FacilityLocationConstraint;

public class ConstraintProviderProxyWrapper implements ConstraintProvider{

    private ConstraintProvider proxy;

    public ConstraintProviderProxyWrapper(){
        FacilityLocationConstraint constraintProvider=new FacilityLocationConstraint();
        ConstraintProviderInvovationHandler handler=new ConstraintProviderInvovationHandler(constraintProvider);
        ConstraintProvider proxy=(ConstraintProvider)Proxy
                .newProxyInstance(constraintProvider.getClass().getClassLoader(),
                constraintProvider.getClass().getInterfaces(), handler);
        this.proxy=proxy;
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return proxy.defineConstraints(constraintFactory);
    }
    
}
