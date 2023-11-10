package cn.keyvalues.optaplanner.common.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

@Schema(description = "约束的定义")
@Data
public class ConstraintDefineVo {

    private Operator operator;
    private String constraintID;

    @Schema(description = "操作对象")
    @Data
    public static class Operator{

        OpTypeEnum opType;
        OpMethod opMethod;
        Operator nextOperator;

    }

    @Schema(description = "操作方法")
    @Data
    public static class OpMethod{
            
        OpMethodEnum method;
        Parameter parameter;

    }

    @Schema(description = "方法参数")
    @Data
    public static class Parameter{

        String className;
        
        /**
         * 一个expression只含一个逻辑运算符
         * 
         * 可以用栈后缀运算，枚举运算符列表
         */
        List<Expression> expressionList;

        String scoreLevel;

        @Schema(description = "逻辑表达式")
        @Data
        public static class Expression{

            // 目前不支持括号
            boolean leftPhrase;
            boolean rightPhrase;

            String expression;
            
            /**
             * && ||
             */
            String connector;
        }

    }

    @Schema(description = "操作类型枚举")
    @Getter
    public static enum OpTypeEnum{
        ConstraintFactory,
        UniConstraintStream,
        UniConstraintBuilder,
    }

    @Schema(description = "操作方法枚举")
    @Getter
    public static enum OpMethodEnum{
        forEach,
        filter,
        penalize,
        reward,
        asConstraint,
    }
    
}