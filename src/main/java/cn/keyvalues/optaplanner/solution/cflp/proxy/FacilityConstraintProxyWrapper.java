package cn.keyvalues.optaplanner.solution.cflp.proxy;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo;
import cn.keyvalues.optaplanner.proxy.handler.ConstraintProviderInvovationHandler;
import cn.keyvalues.optaplanner.solution.cflp.solver.FacilityLocationConstraint;
import cn.keyvalues.optaplanner.utils.planner.ConstraintGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        // 加载自定义约束
        List<Constraint> constraints=null;
        try {
            constraints=loadConstraintDefines(constraintFactory);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        Constraint[] systemDefinedConstraints = proxy.defineConstraints(constraintFactory);
        if(constraints!=null){
            constraints.addAll(Arrays.asList(systemDefinedConstraints));
            return constraints.toArray(new Constraint[0]);
        }else{
            return systemDefinedConstraints;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Constraint> loadConstraintDefines(ConstraintFactory constraintFactory) throws Exception{
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
	    HttpServletRequest request = servletRequestAttributes.getRequest();
        Object defineListObj = request.getAttribute("defineList");
        List<Constraint> list=new ArrayList<>();
        if(defineListObj!=null){
            List<ConstraintDefineVo> defineList=(List<ConstraintDefineVo>)defineListObj;
            for(ConstraintDefineVo define : defineList){
                ConstraintGenerator generator=new ConstraintGenerator(define, constraintFactory);
                Constraint constraint = generator.generate();
                list.add(constraint);
            }
        }
        return list;
    }
    
}
