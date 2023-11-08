package cn.keyvalues.optaplanner.utils.planner;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintBuilder;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintStream;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo.OpMethod;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo.Operator;
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo.Parameter;
import lombok.Data;

/**
 * 约束生成器（客户可视化配置生成约束）
 * <p>结合js生成的结构化对象，进行约束的生成。</p>
 * <p>目前只支持三个可操作对象factory、unistream、unibuilder</p>
 * <p>factory仅支持forEach;unistream仅支持加减分;unibuilder仅支持命名</p>
 */
@Data
public class ConstraintGenerator {

    private ConstraintDefineVo define;

    private Constraint constraint;

    // 通过工厂实现
    private ConstraintFactory constraintFactory;

    private ConstraintStream curStream;
    private ConstraintBuilder curBuilder;
    
    public ConstraintGenerator(ConstraintDefineVo define,ConstraintFactory constraintFactory){
        this.define=define;
        this.constraintFactory=constraintFactory;
    }

    public Constraint generate() throws Exception{
        return generate(define.getOperator());
    }

    private Constraint generate(Operator curPperator) throws Exception{
        if(define==null) throw new IllegalStateException("没有提供约束定义");
        if(constraint!=null) return constraint;
        if(curPperator==null) return constraint;

        // 
        OpMethod opMethod = curPperator.getOpMethod();
        Parameter parameter = opMethod.getParameter();
        ConstraintStream stream=switch(opMethod.getMethod()){
            case forEach->{
                String className = (String)parameter.getMap().get("className");
                Class<?> targetClazz=Class.forName(className);
                yield constraintFactory.forEach(targetClazz);
            }
            case filter->((UniConstraintStream<?>)curStream).filter(t->predicate(t, parameter));
            default->null;
        };
        curStream=stream==null?curStream:stream;
        ConstraintBuilder builder=switch(opMethod.getMethod()){
            case penalize->((UniConstraintStream<?>)curStream).penalize(HardMediumSoftLongScore.ONE_SOFT);
            case reward->((UniConstraintStream<?>)curStream).penalize(HardMediumSoftLongScore.ONE_SOFT);
            default->null;
        };
        curBuilder=builder==null?curBuilder:builder;
        Constraint constraint=switch(opMethod.getMethod()){
            case asConstraint->curBuilder.asConstraint(define.getConstraintID());
            default->null;
        };
        this.constraint=constraint;
        if(constraint!=null){
           return constraint;
        }
        
        return generate(curPperator.getNextOperator());
    }

    private boolean predicate(Object target,Parameter parameter){
        return false;
    }

    // private long penalize(Object target,Parameter parameter){
    //     return 0;
    // }

    // private long reward(Object target,Parameter parameter){
    //     return 0;
    // }

}
