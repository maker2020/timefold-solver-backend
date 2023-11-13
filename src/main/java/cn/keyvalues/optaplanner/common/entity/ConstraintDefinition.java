package cn.keyvalues.optaplanner.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import cn.keyvalues.optaplanner.common.vo.ConstraintDefineVo;
import cn.keyvalues.optaplanner.mybatis.handler.ConstraintDefinitionHandler;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author generator v3.5.3.1
 * @since 2023-11-08
 */
@Getter
@Setter
@TableName(value = "constraint_definition",autoResultMap = true)
@Schema(name = "ConstraintDefinition", description = "$!{table.comment}")
public class ConstraintDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(typeHandler = ConstraintDefinitionHandler.class)
    private ConstraintDefineVo constraintDefinition;

    private String solutionModel;
}
