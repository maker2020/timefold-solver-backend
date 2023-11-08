package cn.keyvalues.optaplanner.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("constraint_definition")
@Schema(name = "ConstraintDefinition", description = "$!{table.comment}")
public class ConstraintDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String constraintDefinition;

    private String solutionModel;
}
