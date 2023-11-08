package cn.keyvalues.optaplanner.aspect;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.keyvalues.optaplanner.common.entity.ConstraintDefinition;
import cn.keyvalues.optaplanner.common.service.IConstraintDefinitionService;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo;
import cn.keyvalues.optaplanner.common.vo.PlannerInputVo;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SolveAspect {

    @Autowired
    IConstraintDefinitionService definitionService;
    
    @Pointcut("execution(* cn.keyvalues.optaplanner.solution..controller..*Controller.solveAsync(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint point){
        Object[] args = point.getArgs();
        PlannerInputVo input=(PlannerInputVo)args[0];
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
	    HttpServletRequest request = servletRequestAttributes.getRequest();
        request.setAttribute("selectedConstraints", input.getSelectedConstraints());

        // 根据切点包名查询对应约束定义
        String solutionModel = point.getSignature().getDeclaringType().getPackageName();
        List<ConstraintDefinition> definitions = definitionService.list(new QueryWrapper<ConstraintDefinition>().eq("solution_model", solutionModel));
        List<ConstraintDefineVo> defineList=definitions.stream().map(entity->JSON.parseObject(entity.getConstraintDefinition(),
                ConstraintDefineVo.class)).toList();
        request.setAttribute("defineList", defineList);
    }

}
