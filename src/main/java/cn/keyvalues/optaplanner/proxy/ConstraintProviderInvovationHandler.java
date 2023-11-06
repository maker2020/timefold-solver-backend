package cn.keyvalues.optaplanner.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class ConstraintProviderInvovationHandler implements InvocationHandler{

    ConstraintProvider target;

    public ConstraintProviderInvovationHandler(ConstraintProvider target){
        this.target=target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 动态配置constraintFactory的过程
        // do nothing
        return method.invoke(target, args);
    }
    
}
