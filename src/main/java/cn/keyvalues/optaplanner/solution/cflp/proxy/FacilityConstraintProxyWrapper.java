package cn.keyvalues.optaplanner.solution.cflp.proxy;

import java.lang.reflect.Proxy;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import cn.keyvalues.optaplanner.proxy.handler.ConstraintProviderInvovationHandler;
import cn.keyvalues.optaplanner.solution.cflp.solver.FacilityLocationConstraint;

public class FacilityConstraintProxyWrapper implements ConstraintProvider{

    private ConstraintProvider proxy;

    public FacilityConstraintProxyWrapper(){
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
