package cn.keyvalues.optaplanner.maprouting.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@Schema(name = "Storehouse", description = "$!{table.comment}")
public class Storehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Integer id;

    private String name;

    private String coordinate;
}
