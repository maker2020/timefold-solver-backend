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
            ExpCalculator calculator=new ExpCalculator();
            return calculator.numberCalculate(exp, target);
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

// for case: (a.c+a.b)+10
/**
 * 不支持计算中含有浮点数、不支持&&，||等逻辑运算。（需要扩展该方法）
 */
class ExpCalculator{

    Stack<String> stack=new Stack<>();
    Map<String,Integer> operatorLevel=new HashMap<>();
    Stack<String> calculateStack=new Stack<>();

    public ExpCalculator(){
        operatorLevel.put("+", 0);
        operatorLevel.put("-", 0);
        operatorLevel.put("*", 1);
        operatorLevel.put("/", 1);
        operatorLevel.put("(", 99);
        operatorLevel.put(")", -99);
    }

    // extra: a.c && a.b
    public boolean logicalCalculate(String s,Object target) throws Exception{
        String[] numberOperators = new String[]{"\\+", "-", "\\*", "/"};
        String regex = String.join("|", numberOperators);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            throw new IllegalStateException("不合法的逻辑运算");
        } else {
            
        }
        return false;
    }

    public static void main(String[] args) throws Exception{
        new ExpCalculator().logicalCalculate("hello", null);
    }

    // for case: (a.c+a.b)+10
    public double numberCalculate(String s,Object target) throws Exception{
        s=parsePropertyValue(s,target);
        String suffixStr = toSuffix(s.trim());
        double right=0,left=0;
        String[] arrS = suffixStr.split(" ");
        for (int i = 0; i < arrS.length; i++) {
            String t = arrS[i];
            if ("".equals(t))
                continue;
            if (NumberUtils.isCreatable(t)) {
                calculateStack.push(t);
            } else {
                if(!calculateStack.empty()) {
                    right = Double.parseDouble(calculateStack.pop());
                }
                if(!calculateStack.empty()){
                    left = Double.parseDouble(calculateStack.pop());
                }else{
                    left = 0;
                }
                switch (t) {
                    case "+"->calculateStack.push((left+right)+"");
                    case "-"->calculateStack.push((left-right)+"");
                    case "*"->calculateStack.push((left*right)+"");
                    case "/"->calculateStack.push((left/right)+"");
                    default->throw new IllegalArgumentException();
                }
            }
        }
        if (calculateStack.size() == 1)
            right = Double.parseDouble(calculateStack.pop());
        return right;
    }

    // 多个则target改为map
    private String parsePropertyValue(String s,Object target) throws Exception{
        String[] ops=new String[]{"+","-","*","/","(",")"};
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

    private String toSuffix(String s) {
         for(int i=0;i<s.length();i++){
            try {
                if("-".equals(s.charAt(i)+"") && "(".equals(s.charAt(i-1)+"")){
                    s=s.substring(0, i)+"0"+s.substring(i, s.length());
                }
            } catch (Exception e) {
                s="0"+s;
            }
        }
        StringBuilder resBuilder = new StringBuilder();
        StringBuilder numberBuilder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 48 && c < 58) {
                numberBuilder.append(c);
                if (i != s.length() - 1)
                    continue;
            }
            resBuilder.append(" " + numberBuilder.toString());
            numberBuilder.setLength(0);
            String cStr = c + "";
            if (operatorLevel.get(cStr) == null)
                continue;
            int level = operatorLevel.get(cStr);
            // )特殊处理
            if(level==-99){
                while (!stack.empty()) {
                    if(operatorLevel.get(stack.peek())!=99){
                        resBuilder.append(" "+stack.pop());
                    }else{
                        stack.pop();
                        break;
                    }
                }
            }else{
                // 优先级小于等于栈顶需要出栈直至大于于
                while (!stack.empty()) {
                    int topLevel = operatorLevel.get(stack.peek());
                    // (特殊处理 ：只接受)才弹出
                    if(topLevel==99&&level!=-99) break;
                    // 大于栈顶
                    if (level > topLevel) {
                        break;
                    }
                    resBuilder.append(" " + stack.pop());
                }
                stack.push(cStr);
            }
        }
        // 全部出栈
        while (!stack.empty()) {
            resBuilder.append(" " + stack.pop());
        }
        return resBuilder.substring(1);
    }

}