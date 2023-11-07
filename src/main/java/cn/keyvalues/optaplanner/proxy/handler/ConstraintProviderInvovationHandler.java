package cn.keyvalues.optaplanner.proxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 由于用户自己配置约束需要中间语言，比较麻烦，暂时只支持用户增删约束
 */
public class ConstraintProviderInvovationHandler implements InvocationHandler{

    ConstraintProvider target;

    public ConstraintProviderInvovationHandler(ConstraintProvider target){
        this.target=target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("defineConstraints")){
            // 动态配置constraintFactory的过程
            // 使用用户输入的约束列表。
            List<String> userConstraints = getUserConstraints();
            if(CollectionUtils.isEmpty(userConstraints)){
                // 程序默认约束列表不变
                return method.invoke(target, args);
            }
            List<Constraint> definedConstraints=new ArrayList<>();
            for(String constraintId:userConstraints){
                Class<? extends ConstraintProvider> clazz=target.getClass();
                Method constraintDefinedMethod = clazz.getDeclaredMethod(constraintId, ConstraintFactory.class);
                constraintDefinedMethod.setAccessible(true);
                Constraint constraint = (Constraint)constraintDefinedMethod.invoke(target, args);
                definedConstraints.add(constraint);
            }
            return definedConstraints.toArray(new Constraint[0]);
        }else{
            return method.invoke(target, args);   
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserConstraints() throws Exception{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
	    HttpServletRequest request = servletRequestAttributes.getRequest();
        Object attribute = request.getAttribute("selectedConstraints");
        if(attribute==null) return null;
        List<String> constrains = (List<String>)attribute;
        return constrains;
    }
    
}
