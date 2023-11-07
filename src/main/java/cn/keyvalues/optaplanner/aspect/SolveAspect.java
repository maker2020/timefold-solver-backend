package cn.keyvalues.optaplanner.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.keyvalues.optaplanner.common.vo.PlannerInputVo;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SolveAspect {
    
    @Pointcut("execution(* cn.keyvalues.optaplanner.solution..controller..*Controller.solveAsync(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint point){
        Object[] args = point.getArgs();
        PlannerInputVo input=(PlannerInputVo)args[0];
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
	    HttpServletRequest request = servletRequestAttributes.getRequest();
        request.setAttribute("selectedConstraints", input.getSelectedConstraints());
    }

}
