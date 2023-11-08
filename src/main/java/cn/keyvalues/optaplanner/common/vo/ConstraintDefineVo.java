package cn.keyvalues.optaplanner.common.vo;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

@Schema(description = "约束的定义")
@Data
public class ConstraintDefineVo {

    private Operator operator;
    private String constraintID;

    @Schema(description = "操作对象")
    @Getter
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

        Map<String,Object> map;

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