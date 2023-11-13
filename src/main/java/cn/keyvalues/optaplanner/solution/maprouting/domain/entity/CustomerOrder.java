package cn.keyvalues.optaplanner.solution.maprouting.domain.entity;

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
 * @since 2023-10-08
 */
@Getter
@Setter
@TableName("customer_order")
@Schema(name = "CustomerOrder", description = "$!{table.comment}")
public class CustomerOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    private String coordinate;

    private Boolean disabled;

    private String address;

    private String citycode;
}
