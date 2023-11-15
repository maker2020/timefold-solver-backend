package cn.keyvalues.optaplanner.utils.planner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
            case penalize->((UniConstraintStream<?>)curStream).penalize(scoreType(parameter),t->weightFunction(t, parameter));
            case reward->((UniConstraintStream<?>)curStream).reward(scoreType(parameter),t->weightFunction(t, parameter));
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
        expression.setExpression("t.maxDemand>10");
        
        List<Expression> list=new ArrayList<>();
        list.add(expression);

        parameter.setExpressionList(list);
        System.out.println(predicate(c, parameter));
    }

    static HardMediumSoftLongScore scoreType(Parameter parameter){
        String scoreLevel = parameter.getScoreLevel();
        return switch (scoreLevel) {
            case "SOFT"->HardMediumSoftLongScore.ONE_SOFT;
            case "MEDIUM"->HardMediumSoftLongScore.ONE_MEDIUM;
            case "HARD"->HardMediumSoftLongScore.ONE_HARD;
            default->HardMediumSoftLongScore.ONE_SOFT;
        };
    }

    static int weightFunction(Object t,Parameter parameter){
        List<Expression> expressionList = parameter.getExpressionList();
        if(CollectionUtils.isEmpty(expressionList)) {
            return 1;
        }
        if(expressionList.size()>1) {
            throw new IllegalStateException();
        }
        Expression expression = expressionList.get(0);
        String exp = expression.getExpression();
        if(!StringUtils.hasLength(exp)){
            return 1;
        }
        Object expValue;
        try {
            expValue = getExpValue(t, exp);
            if(!(expValue instanceof Number)){
                throw new IllegalStateException("权重表达式必须返回数字");
            }
            return ((Number)expValue).intValue();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e){
            throw new IllegalArgumentException("分数权重解析失败");
        }
    }

    /**
     * 目前demo全按二元算式处理，后改为连续
     */
    static boolean predicate(Object target,Parameter parameter){
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
    static boolean getExpressionValue(Object target,String expression) throws Exception{
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
     * 子表达式(复合式子)
     */
    static Object getExpValue(Object target,String exp) throws Exception{
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
        
        
        String regex = ".*\\d.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(exp);
        if (matcher.matches()) {
            ExpressCalculator calculator=new ExpressCalculator();
            return calculator.calculateExp(target, exp);
        }else{
            return getPropertyValue(target, exp);
            // throw new UnsupportedOperationException();
        }
    }

    static Object getPropertyValue(Object target,String propertyExp) throws Exception{
        Class<?> clazz = target.getClass();
        String[] expProperties = propertyExp.split("\\.");
        Object obj=target;
        for (int i=0;i<expProperties.length;i++) {
            if(i==0) continue;
            String property=expProperties[i];
            Field field = clazz.getDeclaredField(property);
            field.setAccessible(true);
            obj=field.get(obj);
            field.setAccessible(false);
        }
        return obj;
    }

}


/**
 * 支持成员运算，数字运算，逻辑运算，及某个类型的运算的复合运算。
 */
class ExpressCalculator {

    static final String split_operator_regex;
    
    static final Map<String,Integer> operatorLevel=new HashMap<>();

    static{
        operatorLevel.put("+", 0);
        operatorLevel.put("-", 0);
        operatorLevel.put("*", 1);
        operatorLevel.put("/", 1);
        operatorLevel.put("&&", 10);
        operatorLevel.put("||", 10);
        operatorLevel.put("(", 99);
        operatorLevel.put(")", -99);

        String[] ops=new String[]{"+","-","*","/","(",")","&&","||"};
        split_operator_regex = String.join("|", Arrays.stream(ops)
                .map(Pattern::quote) // 对特殊字符进行转义
                .toArray(String[]::new));
    }

    Stack<String> operatorStack=new Stack<>();
    Stack<String> calculateStack=new Stack<>();

    /**
     * human.money+9999>100000 or human.isHandsome && human.isRich
     * @param target 操作对象 (human or ... or null)
     * @param exp 成员运算、数字运算、逻辑运算表达式
     * @return
     */
    public Object calculateExp(Object target,String exp) throws Exception{
        // 将表达式中成员运算解析为对象实际存储值，并得到新表达式
        String newExp = parsePropertyValue(exp, target);
        return calculate(toSuffix(newExp));
    }

    /**
     * 转后缀式
     * @param expression for example: 2*2.5+(3-1)、true && (false || true)
     * @return
     */
    public List<String> toSuffix(String expression){
        List<String> suffixList=new ArrayList<>();
        List<String> elements = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?|true|false|&&|\\|\\||[+\\-*/()]");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            elements.add(matcher.group().trim());
        }
        // 生成后缀式
        for(int i=0;i<elements.size();i++){
            String element=elements.get(i);
            if(isNumber(element) || isLogicValue(element)){
                suffixList.add(element);
                continue;
            }
            // 负号还是减号转义特殊处理
            if(element.equals("-") && i!=0 
                    && elements.get(i-1).equals("(")){
                suffixList.add("0");
            }
            if(element.equals(")")){
                while (!operatorStack.empty()) {
                    if(!operatorStack.peek().equals("(")){
                        suffixList.add(operatorStack.pop());
                    }else{
                        operatorStack.pop();
                        break;
                    }
                }
            }else{
                // 优先级小于等于栈顶需要出栈直至大于于
                while (!operatorStack.empty()) {
                    String operator= operatorStack.peek();
                    // (特殊处理 ：只接受)才弹出
                    if(operator.equals("(")&&!element.equals(")")) {
                        break;
                    }
                    // 大于栈顶
                    if (operatorLevel.get(element) > operatorLevel.get(operator)) {
                        break;
                    }
                    suffixList.add(operatorStack.pop());
                }
                operatorStack.push(element);
            }
        }
        // 全部出栈
        while (!operatorStack.empty()) {
            suffixList.add(operatorStack.pop());
        }
        return suffixList;
    }

    /**
     * 计算后缀式
     * @return boolean or number
     */
    public Object calculate(List<String> suffixList){
        if(suffixList.contains("&&")||suffixList.contains("||")){ // 逻辑运算
            Boolean right=false,left=false;
            for(String element:suffixList){
                if(isLogicValue(element)){
                    calculateStack.push(element);
                }else{
                    if(!calculateStack.empty()){
                        right=Boolean.parseBoolean(calculateStack.pop());
                    }
                    if(!calculateStack.empty()){
                        left=Boolean.parseBoolean(calculateStack.pop());
                    }
                    switch(element){
                        case "&&"->calculateStack.push(Boolean.toString(left&&right));
                        case "||"->calculateStack.push(Boolean.toString(left||right));
                    }
                }
            }
            if(calculateStack.size()==1){
                right=Boolean.parseBoolean(calculateStack.pop());
            }
            return right;
        }else{
            double right=0,left=0;
            for(String element:suffixList){
                if(isNumber(element)){
                    calculateStack.push(element);
                }else{
                    if(!calculateStack.empty()) {
                        right = Double.parseDouble(calculateStack.pop());
                    }
                    if(!calculateStack.empty()){
                        left = Double.parseDouble(calculateStack.pop());
                    }else{
                        left = 0;
                    }
                    switch (element) {
                        case "+"->calculateStack.push((left+right)+"");
                        case "-"->calculateStack.push((left-right)+"");
                        case "*"->calculateStack.push((left*right)+"");
                        case "/"->calculateStack.push((left/right)+"");
                        default->throw new IllegalArgumentException();
                    }
                }
            }
            if(calculateStack.size()==1){
                right=Double.parseDouble(calculateStack.pop());
            }
            return right;
        }
    }

    /**
     * 从表达式s解析出target对应属性值，并替换到原始字符串中去。
     * @param s
     * @param target
     * @return
     * @throws Exception
     */
    private String parsePropertyValue(String s,Object target) throws Exception{
        String[] ops=new String[]{"+","-","*","/","(",")","&&","||"};
        String regex = String.join("|", Arrays.stream(ops)
                .map(Pattern::quote) // 对特殊字符进行转义
                .toArray(String[]::new));
        String[] split = s.split(regex);
        for(String item:split){
            if(item.contains(".")){
                Object propertyValue = ConstraintGenerator.getPropertyValue(target,item);
                s=s.replace(item, propertyValue.toString());
            }
        }
        return s;
    }

    // private Object getPropertyValue(Object target,String propertyExp) throws Exception{
    //     Class<?> clazz = target.getClass();
    //     String[] expProperties = propertyExp.split("\\.");
    //     Object obj=target;
    //     for (int i=0;i<expProperties.length;i++) {
    //         if(i==0) continue;
    //         String property=expProperties[i];
    //         Field field = clazz.getDeclaredField(property);
    //         field.setAccessible(true);
    //         obj=field.get(obj);
    //         field.setAccessible(false);
    //     }
    //     return obj;
    // }

    private boolean isNumber(String str){
        try{
            Double.parseDouble(str);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    private boolean isLogicValue(String str){
        return "true".equals(str)||"false".equals(str);
    }

}
