package cn.keyvalues.optaplanner.utils.planner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

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
import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo.Parameter.Expression;
import cn.keyvalues.optaplanner.solution.cflp.domain.Customer;
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

    private Constraint generate(Operator curOperator) throws Exception{
        if(define==null) throw new IllegalStateException("没有提供约束定义");
        if(constraint!=null) return constraint;
        if(curOperator==null) return constraint;

        // 
        OpMethod opMethod = curOperator.getOpMethod();
        Parameter parameter = opMethod.getParameter();
        ConstraintStream stream=switch(opMethod.getMethod()){
            case forEach->{
                String className = parameter.getClassName();
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
        
        return generate(curOperator.getNextOperator());
    }

    public static void main(String[] args) throws Exception {
        Customer c=new Customer();
        c.setMaxDemand(10);
        Parameter parameter=new Parameter();
        parameter.setClassName("cn.keyvalues.optaplanner.solution.cflp.domain.Customer");
        Expression expression=new Expression();
        expression.setConnector("&&");
        expression.setExpression("t.maxDemand>1");
        
        List<Expression> list=new ArrayList<>();
        list.add(expression);

        parameter.setExpressionList(list);
        System.out.println(predicate(c, parameter));
    }

    /**
     * 目前demo全按二元算式处理，后改为连续
     */
    private static boolean predicate(Object target,Parameter parameter){
        try{
            List<Expression> expressionList = parameter.getExpressionList();

            boolean res=getExpressionValue(target,expressionList.get(0).getExpression());
            
            // 没考虑加括号、优先级问题
            for(int i=1;i<expressionList.size();i++){
                Expression expression = expressionList.get(i);
                boolean expressionValue = getExpressionValue(target, expression.getExpression());
                String connector = expression.getConnector();
                if("&&".equals(connector)){
                    res=res&&expressionValue;
                } else if("||".equals(connector)){
                    res=res||expressionValue;
                }
            }
            return res;
        }catch(Exception e){
            return false;
        } 
    }

    /**
     * sample: a.b>a.c , a.b>100
     * expression example: (a.b+a.c)+10 > 100
     */
    private static boolean getExpressionValue(Object target,String expression) throws Exception{
        String[] logicalOperators=new String[]{">","<","=",">=","<=","&&","||"};
        String regex = String.join("|", Arrays.stream(logicalOperators)
                .map(Pattern::quote) // 对特殊字符进行转义
                .toArray(String[]::new));
        String[] childExps = expression.split(regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);
        String logicalOperator=null;
        if(matcher.find()){
            logicalOperator=matcher.group();
        }
        if(logicalOperator==null){ // 则表达式必须是boolean结果
            Object expValue = getExpValue(target, childExps[0]);
            if(!expValue.getClass().getTypeName().equals("java.lang.Boolean")){
                throw new IllegalStateException();
            }
        }
        Object expValue = getExpValue(target, childExps[0]);
        Object expValue2 = getExpValue(target, childExps[1]);
        boolean res=switch (logicalOperator) {
            case ">"->((Number)expValue).doubleValue()>((Number)expValue2).doubleValue();
            case "<"->((Number)expValue).doubleValue()<((Number)expValue2).doubleValue();
            case ">="->((Number)expValue).doubleValue()>=((Number)expValue2).doubleValue();
            case "<="->((Number)expValue).doubleValue()<=((Number)expValue2).doubleValue();
            case "="->((Number)expValue).doubleValue()==((Number)expValue2).doubleValue();
            case "&&"->(boolean)expValue && (boolean)expValue2;
            case "||"->(boolean)expValue || (boolean)expValue2;
            default->throw new IllegalStateException();
        };
        return res;
    }

    /**
     * 子表达式 (demo先简单实现，不考虑复合式子)
     */
    private static Object getExpValue(Object target,String exp) throws Exception{
        if(!exp.contains(".")){
            // number or boolean
            if(NumberUtils.isCreatable(exp)){
                return Double.parseDouble(exp);
            }
            if(exp.toUpperCase().equals("TRUE") ||
                    exp.toUpperCase().equals("FALSE")){
                return Boolean.parseBoolean(exp);
            }
            throw new IllegalArgumentException();
        }
        
        return getPropertyValue(target, exp);
    }
    

    private static Object getPropertyValue(Object target,String propertyExp) throws Exception{
        Class<?> clazz = target.getClass();
        String[] expProperties = propertyExp.split("\\.");
        Object obj=target;
        for (int i=0;i<expProperties.length;i++) {
            if(i==0) continue;
            String property=expProperties[i];
            Field field = clazz.getDeclaredField(property);
            field.setAccessible(true);
            obj=field.get(obj);
        }
        return obj;
    }

    // private long penalize(Object target,Parameter parameter){
    //     return 0;
    // }

    // private long reward(Object target,Parameter parameter){
    //     return 0;
    // }

}
